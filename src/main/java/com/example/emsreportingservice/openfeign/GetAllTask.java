package com.example.emsreportingservice.openfeign;

import com.example.emsreportingservice.dto.RequestListUUidsDto;
import com.example.emsreportingservice.dto.TaskModelDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "getAllTask", url = "${TASK_URL}")
public interface GetAllTask {

 @PostMapping(path = "/api/v1/getAllbyId")
  ResponseEntity<List<TaskModelDto>> getAllById(@Valid @RequestBody RequestListUUidsDto requestListUUidsDto);
}
