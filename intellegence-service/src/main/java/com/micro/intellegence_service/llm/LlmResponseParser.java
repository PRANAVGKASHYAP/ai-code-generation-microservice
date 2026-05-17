package com.micro.intellegence_service.llm;

import com.micro.common_lib.enums.ChatEventStatus;
import com.micro.common_lib.enums.ChatEventType;
import com.micro.intellegence_service.entity.ChatEvent;
import com.micro.intellegence_service.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class LlmResponseParser {
    // this clas will be used in extracting all teh different tags present in the llm response like content , tool calls , file operations etc...

    //Pattern 1 -> match the tage (message / tool / file)
    private static final Pattern TAG_MATCHER = Pattern.compile(
            "(<(message|file|tool)([^>]*)>)([\\s\\S]*?)(</\\2>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ATTRIBUTE_MATCHER = Pattern.compile(
            "(path|args)=\"([^\"]+)\""
    );

    public Map<String , String> extractAttributes(String response){

        Map<String , String> attributes= new HashMap<String,String>();
        if (response == null){
            return attributes;
        }
        Matcher attrMatcher = ATTRIBUTE_MATCHER.matcher(response);

        while (attrMatcher.find()){
            attributes.put(attrMatcher.group(1) , attrMatcher.group(2));
        }

        return attributes;
    }

    public List<ChatEvent> parseResponse(String fullResponse , ChatMessage parentMessage){

        List<ChatEvent> events = new ArrayList<>();
        int orderCount = 1; // this is the sequence order

        Matcher tagMatcher = TAG_MATCHER.matcher(fullResponse);
        while (tagMatcher.find()){
            //extract all teh matched component from this matcher like attribute , content etc ...
            String tagName = tagMatcher.group(2).toLowerCase();
            String attr =  tagMatcher.group(3);
            String content = tagMatcher.group(4).trim();

            Map<String , String> attributes = extractAttributes(attr);

            // collect all teh attributes , content and the event typ and store it in db
            ChatEvent.ChatEventBuilder currEvent = ChatEvent.builder()
                    .chatMessage(parentMessage)
                    .content(content)
                    .status(ChatEventStatus.COMPLETED)
                    .sequenceOrder(orderCount++);
            // descide which type of event this is
            switch (tagName){
                case "message" -> currEvent.chatEventType(ChatEventType.MESSAGE);
                case "file" ->{
                    currEvent.chatEventType(ChatEventType.FILE_EDIT);
                    currEvent.status(ChatEventStatus.PENDING); // only for the file edit events the status is pending and we need a saga id
                    currEvent.filePath(attributes.get("path"));
                }
                case "tool" -> {
                    currEvent.chatEventType(ChatEventType.TOOL_LOG);
                    currEvent.metadata(attributes.get("args"));
                }
                default -> {continue;}
            }

            events.add(currEvent.build());
        }
        return events;
    }
}
