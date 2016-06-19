package com.vaidya;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.users.FullAccount;

@Configuration
@EnableBatchProcessing
@PropertySource("classpath:application.properties")


public class UpdateAccounts {
	
	@Value("${pimFileURL}")
	private String pimFileURL;
	
    private static final String ACCESS_TOKEN = "28Ws0Ut0fxkAAAAAAAAeknWCSXrqa3eMwQaOx4JI5iPiEW_6GBYJ4Oi3cPTY_0t4";

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	// public DataSource dataSource;

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Accounts> reader() throws DbxException, IOException {
		this.downLoadPIMFile();
		FlatFileItemReader<Accounts> reader = new FlatFileItemReader<Accounts>();
		reader.setResource(new ClassPathResource("PIM DB 01.csv"));
		reader.setLinesToSkip(1);
		reader.setLineMapper(new DefaultLineMapper<Accounts>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "belongs", "number", "category", "description", "valid_from",
								"valid_to", "address", "name", "web_address", "uid", "pwd", "hash_key" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Accounts>() {
					{
						setTargetType(Accounts.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public AccountsItemProcessor processor() {
		return new AccountsItemProcessor();
	}

	private boolean downLoadPIMFile() throws DbxException, IOException {
		
		DbxRequestConfig config = new DbxRequestConfig("Vaidya-Dropbox");
//        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        
        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        System.out.println(account.getName().getDisplayName());

        // Get files and folder metadata from Dropbox root directory
           DbxDownloader downloader = client.files().download(pimFileURL);
           File testFile = new File("PIM.xlsx");
           testFile.createNewFile();
           FileOutputStream jpegStream = new FileOutputStream(testFile);
           downloader.download(jpegStream);
           jpegStream.close();
           
           return true;

	}

	// end::readerwriterprocessor[]

	// tag::listener[]

	@Bean
	public AccountsRedisItemWriter<Accounts> writer() {
		return new AccountsRedisItemWriter<>();
	}

	// end::listener[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob() {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
	}

	@Bean
	public Step step1() {
		
		Step firstStep = null;
		try {
			firstStep =  stepBuilderFactory.get("step1").<Accounts, Accounts> chunk(10).reader(reader()).processor(processor())
					.writer(writer()).build();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			return firstStep;
		}
		
	}
	// end::jobstep[]
}