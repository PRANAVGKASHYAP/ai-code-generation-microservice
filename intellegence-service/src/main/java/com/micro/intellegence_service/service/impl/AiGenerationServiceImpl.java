package com.micro.intellegence_service.service.impl;


import com.micro.common_lib.enums.ChatEventType;
import com.micro.common_lib.enums.MessageRole;
import com.micro.common_lib.error.ResourceNotFoundException;
import com.micro.common_lib.event.FileStoreRequestEvent;
import com.micro.common_lib.security.AuthUtil;
import com.micro.intellegence_service.client.WorkspaceServiceClient;
import com.micro.intellegence_service.entity.ChatEvent;
import com.micro.intellegence_service.entity.ChatMessage;
import com.micro.intellegence_service.entity.ChatSession;
import com.micro.intellegence_service.entity.ChatSessionId;
import com.micro.intellegence_service.llm.LlmResponseParser;
import com.micro.intellegence_service.llm.PromptUtils;
import com.micro.intellegence_service.llm.advisors.FileTreeContextAdvisor;
import com.micro.intellegence_service.llm.tools.CodeGenerationTools;
import com.micro.intellegence_service.repository.ChatEventRepository;
import com.micro.intellegence_service.repository.ChatMessageReopsitory;
import com.micro.intellegence_service.repository.ChatSessionRepository;
import com.micro.intellegence_service.repository.UsageLogRepository;
import com.micro.intellegence_service.service.AiGenerationService;
import com.micro.intellegence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient; // this wil load the open router model specified in the yaml file
    //private final SecurityExpression securityExpression;
    private final AuthUtil authUtil;
    //private final FileService fileService; // this is the service for project files
    private final PromptUtils promptUtils;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final LlmResponseParser llmResponseParser;
    private final ChatSessionRepository chatSessionRepository;
    //private final ProjectRepository projectRepository;
    //private final UserRepository userRepository;
    private final ChatMessageReopsitory chatMessageReopsitory;
    private final ChatEventRepository chatEventRepository;
    private final UsageLogRepository usageLogRepository;
    private final UsageService usageService;
    private final WorkspaceServiceClient workspaceServiceClient;

    // use a kafka template to use kafka events
    private final KafkaTemplate<String , Object> kafkaTemplate;

    private static Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>" , Pattern.DOTALL);
    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String message, Long projectId) {

        // before sending teh prompt ot the llm check id the daily usage limit has been crossed
        usageService.checkDailyUsage();


        // initialize the time variable as atomic as it will be accessed by many threads
        AtomicReference<Long>startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long>endTime = new AtomicReference<>(0L);
        AtomicReference<Usage>usageData = new AtomicReference<>();
        Long userId = authUtil.getCurrentUserId();
        // check if there is a chat session for this user in this project

        ChatSession session =  createChatSessionIfNotPresent(userId , projectId);
        // configure and create a chat client object
        Map<String , Object>mp = Map.of(
                "userId" , userId ,
                "projectId" , projectId
        );

        // creating an object og the code generation tools explicitly passing the project id and the path
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(workspaceServiceClient , projectId);

        StringBuilder sb = new StringBuilder(); // this is to buffer the llm response
        String system_prompt = PromptUtils.CODE_GENERATION_SYSTEM_PROMPT;
        return chatClient.prompt()
                .system(system_prompt)
                .user(message)
                .tools(codeGenerationTools)
                .advisors(
                        advisorSpec -> {
                            advisorSpec.params(mp);
                            advisorSpec.advisors(fileTreeContextAdvisor);
                        }
                        // add the file tree advisor for the llm to get info on the project folder structure
                )
                .stream()
                .chatResponse()
                .doOnNext(response -> {

                    // update the end time here
                    String chunk = response.getResult().getOutput().getText();
                    if(chunk != null && !chunk.isEmpty()){
                        endTime.set(System.currentTimeMillis());
                    }
                    sb.append(chunk);
                    // extract teh tokens used
                    if(response.getMetadata().getUsage() != null){
                        usageData.set(response.getMetadata().getUsage());
                    }
                })
                .doOnComplete( () -> {
                    // here the string buffer will now have the full response
                    Long thinkingTime = (endTime.get()  - startTime.get() )/1000;
                    Schedulers.boundedElastic().schedule(() -> {
                        //parseTheLLMResponse(sb.toString() , projectId);
                        finalizeResponse(message , session , sb.toString() , projectId , thinkingTime , usageData.get());
                    });
                })
                .doOnError(System.out::println)
                .map(chatResponse -> Objects.requireNonNull(chatResponse.getResult().getOutput().getText()));

    }

//    private void parseTheLLMResponse(String string, Long projectId) {
//        // the string will be in teh format of tags , with tags like <message>  , <file> ...etc
//        Matcher matcher = FILE_TAG_PATTERN.matcher(string);
//
//        while(matcher.find()){
//            // if the code enters this loop then text file <file path=..> ... </file> is present in the text
//            String filePath = matcher.group(1);
//            String fileContent = matcher.group(2).trim(); // save this file in the min io
//
//            //fileService.saveFile(projectId , filePath , fileContent);
//
//        }
//    }

    private void finalizeResponse(String userMessage , ChatSession chatSession , String fullResponse , Long projectId , Long duration , Usage usage){

        Long currProjectId = chatSession.getId().getProjectId();
        // the usage will have the prompt tokens and also the completion tokens
        if(usage != null){
            int totalTokens = usage.getTotalTokens();
            usageService.recordTokenUsage(chatSession.getId().getUserId(),  totalTokens);
        }
        // update teh usage logs for this chat of teh user
        //1. save the user message
        ChatMessage currUserMessage = ChatMessage.builder()
                .chatSession(chatSession)
                .content(userMessage)
                .messageRole(MessageRole.USER)
                .tokensUsed(usage.getPromptTokens())
                .build();
        chatMessageReopsitory.save(currUserMessage);

        //2. create the llm message
        ChatMessage llmMessage = ChatMessage.builder()
                .messageRole(MessageRole.LLM)
                .content("LLM RESPONSE")
                .chatSession(chatSession)
                .tokensUsed(usage.getCompletionTokens())
                .build();
        llmMessage = chatMessageReopsitory.save(llmMessage);

        List<ChatEvent> events = llmResponseParser.parseResponse(fullResponse , llmMessage);
        events.addFirst(
                ChatEvent.builder()
                        .chatEventType(ChatEventType.THOUGHT)
                        .chatMessage(llmMessage)
                        .content("Thought for " + duration.toString() + "seconds")
                        .sequenceOrder(0)
                        .build()
        );
        events.stream()
                .filter(ele -> ele.getChatEventType() == ChatEventType.FILE_EDIT)
                // insted of using the file service fomr this file to save the files , we will use kafka , to emmit a file edit event
                .forEach(
                        // TODO  use kafka here to send the file edit event
                        ele -> {
                            // create an object of FileStoreRequest ad send it using kafka
                            String uuid = UUID.randomUUID().toString();
                            ele.setSagaId(uuid);
                            FileStoreRequestEvent fileStoreRequestEvent = new FileStoreRequestEvent(
                                    projectId , uuid , ele.getFilePath() , ele.getContent()
                            );
                            kafkaTemplate.send("file-storage-event" , "project:" + projectId , fileStoreRequestEvent);
                        } //fileService.saveFile(projectId , ele.getFilePath() , ele.getContent())
                );

        chatEventRepository.saveAll(events);
    }

    private ChatSession createChatSessionIfNotPresent(Long userId, Long projectId) {
        ChatSessionId id = new ChatSessionId(userId , projectId);
        ChatSession session = chatSessionRepository.findById(id).orElse(null);
//        Project project = projectRepository.findById(projectId).orElseThrow(
//                () -> new ResourceNotFoundException("Project with id " + projectId + "not found")
//        );
//        User user = userRepository.findById(userId).orElseThrow(
//                () -> new ResourceNotFoundException("user with id " + userId + "not found")
//        );
        if(session == null){
            //create a new session and store it in db
            ChatSession newSession = ChatSession.builder()
                    .id(id)
                    .createdAt(Instant.now())
//                    .project(project)
//                    .user(user)
                    .build();
            chatSessionRepository.save(newSession);
            return newSession;
        }

        return session;
    }
}
