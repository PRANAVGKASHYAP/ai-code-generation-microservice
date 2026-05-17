package com.micro.intellegence_service.llm.tools;

import com.micro.intellegence_service.client.WorkspaceServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTools {

    private final WorkspaceServiceClient workspaceServiceClient; // this is the project file service
    private final Long projectId; // this will be explicitly passed using teh constrictor

    @Tool(name = "read_files",
            description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(
            @ToolParam(description = "List of relative paths (e.g., ['src/App.tsx'])")
            List<String> paths){

        List<String> resultArr = new ArrayList<>();

        for(String path : paths){
            String validPath = path.startsWith("/")? path.substring(1): path;

            // now call the minio client to get teh actual data at this file path
            String content = workspaceServiceClient.getFileContent(projectId , validPath).content();

            resultArr.add(
                    String.format("The start of the file with path : %s : \n %s \n ----- end of the file "
                     , validPath , content)
            );

            log.info("The response from the file read tool call is this---------------------->\n\n\n " + resultArr.toString() + "\n\n");
        }


        return resultArr;
    }
}
