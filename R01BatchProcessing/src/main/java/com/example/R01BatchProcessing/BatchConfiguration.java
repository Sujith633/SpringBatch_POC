package com.example.R01BatchProcessing;


import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration

public class BatchConfiguration {
	
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public DataSourceTransactionManager transactionManager() {
	
		return new DataSourceTransactionManager(dataSource);
	}
	
	@Bean
	public FlatFileItemReader<Person> reader() {
		System.out.print("Reader");
	  return new FlatFileItemReaderBuilder<Person>()
	    .name("personItemReader")
	    .resource(new ClassPathResource("inputdata.csv"))
	    .delimited()
	    .names("firstName", "lastName")
	    .targetType(Person.class)
	    .build();
	}
	
	@Bean
	public PersonItemProcessor processor() {
		System.out.print("Processor");
	  return new PersonItemProcessor();
	}
	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		System.out.println("Writer");
	  return new JdbcBatchItemWriterBuilder<Person>()
	    .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
	    .dataSource(dataSource)
	    .beanMapped()
	    .build();
	}
	
	@Bean
	public Job importUserJob(JobRepository jobRepository,Step step1, JobCompletionNotificationListener listener) {
		System.out.println("Job Repository");
	  return new JobBuilder("importUserJob", jobRepository)
	    .listener(listener)
	    .start(step1)
	    .build();
	}

	@Bean
	public Step step1(JobRepository jobRepository, DataSourceTransactionManager transactionManager,
	          FlatFileItemReader<Person> reader, PersonItemProcessor processor, JdbcBatchItemWriter<Person> writer) {
		System.out.println("step1");
	  return new StepBuilder("step1", jobRepository)
	    .<Person, Person> chunk(3, transactionManager)
	    .reader(reader)
	    .processor(processor)
	    .writer(writer)
	    .build();
	}
}