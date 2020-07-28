package com.example.transitsystem.service.task;

import com.example.transitsystem.service.PicManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClearFileEveryDay {

    @Autowired
    PicManagerService picManagerService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void doClearFile() {
        log.info("执行清空文件，清空可能因为系统异常而产生的临时文件");
        picManagerService.doClearFile();
    }

}
