package com.micro.intellegence_service.controller;



import com.micro.intellegence_service.dto.ChatRequest;
import com.micro.intellegence_service.service.AiGenerationService;
import com.micro.intellegence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    // here there needs to be a chat endpoint
    private final AiGenerationService aiGenerationService;
    private final ChatService chatService;

    @PostMapping(value = "/stream" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>>streamChat(
            @RequestBody ChatRequest request
    ){

        return aiGenerationService.streamResponse(request.message() , request.projectId())
                .map(
                        data -> ServerSentEvent.<String>builder()
                                .data(data)
                                .build()
                )
                ;

    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable("projectId") Long projectId
    ){
        List<ChatResponse> resp =  chatService.getProjectChatHistory(projectId);
        return ResponseEntity.ok(resp);
    }


}
