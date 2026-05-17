package com.micro.intellegence_service.llm.advisors;


import com.micro.intellegence_service.client.WorkspaceServiceClient;
import com.micro.intellegence_service.dto.FileNode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {

    private final WorkspaceServiceClient workspaceServiceClient;

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Map<String , Object> context = chatClientRequest.context();
        Long projectId = Long.valueOf(context.getOrDefault("projectId" , 0).toString());
        Long userId = Long.valueOf(context.getOrDefault("userId" , 0).toString());

        // this is the function tht will actually return the file tree data

        ChatClientRequest fileTreeAugmentedRequest = addFileTreeToPrompt(chatClientRequest , projectId);

        return streamAdvisorChain.nextStream(fileTreeAugmentedRequest);
    }

    public ChatClientRequest addFileTreeToPrompt(ChatClientRequest request , Long projectId){

        List<Message> curentMessage = request.prompt().getInstructions();

        Message systemMessage = curentMessage.stream()
                .filter(ele -> ele.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElse(null);
        List<Message> userMesassage = curentMessage.stream()
                .filter(ele -> ele.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<Message> totalMessages = new ArrayList<>();

        //1 first add the system messages
        if(systemMessage != null)
        {totalMessages.add(systemMessage);}

        List<com.micro.intellegence_service.dto.FileNode> fileNodes =  workspaceServiceClient.getFileTree(projectId).files();

        String augmentedContext = "\n\n FILE TREE \n\n" + fileNodes.toString();
        totalMessages.add(new SystemMessage(augmentedContext));
        totalMessages.addAll(userMesassage);

        return request.mutate()
                .prompt(new Prompt(totalMessages , request.prompt().getOptions()))
                .build();
    }

    @Override
    public String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // this is the index at which the advisors are passed to the llm
    }
}
