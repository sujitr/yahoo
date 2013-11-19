package com.sujit.dashexp.scheduler.artifacts;

import static org.quartz.JobBuilder.newJob;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class CronScheduler {
	public CronScheduler() throws SchedulerException{
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();
		JobDetail job = newJob(ReportJob.class).withIdentity("job1", "group1").build();
		CronTrigger trigger = TriggerBuilder.newTrigger()
        .withIdentity("job1", "group1")
        //.withSchedule(CronScheduleBuilder.cronSchedule("*/10 * * * * ?"))
        .withSchedule(CronScheduleBuilder.cronSchedule("0 */15 * * * ?"))
        .build();
		scheduler.scheduleJob(job, trigger);
	}
}
