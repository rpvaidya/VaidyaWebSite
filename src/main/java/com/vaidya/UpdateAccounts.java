package com.vaidya;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.hibernate.jpa.boot.spi.InputStreamAccess;
//import org.opensaml.util.resource.ClasspathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${pimFileURL}")
	private String pimFileURL;

	private static final String ACCESS_TOKEN = "28Ws0Ut0fxkAAAAAAAAeknWCSXrqa3eMwQaOx4JI5iPiEW_6GBYJ4Oi3cPTY_0t4";

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private ServletContext appContext;

	@Autowired
	// public DataSource dataSource;

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Accounts> reader() throws IOException  {
		String filePath = this.downLoadPIMFile();
//		if (filePath.length() == 0) {
	//		
		//	log.error("File not downloaded from drop box. Using the last downloaded file");
		//}
		
		FlatFileItemReader<Accounts> reader = new FlatFileItemReader<Accounts>();
		Resource resource = new FileSystemResource("src/main/resources/Accounts.csv");		
		reader.setResource(resource);
		
//		reader.setResource(new ClassPathResource("Accounts.csv"));
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

	private String downLoadPIMFile() {

		DbxRequestConfig config = new DbxRequestConfig("Vaidya-Dropbox");
		// DbxRequestConfig config = new
		// DbxRequestConfig("dropbox/java-tutorial", "");
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		String filePath = "";
		// Get current account info
		FullAccount account;
		try {
			account = client.users().getCurrentAccount();
			System.out.println(account.getName().getDisplayName());

			// Get files and folder metadata from Dropbox root directory
			DbxDownloader downloader = client.files().download(pimFileURL);
			File testFile = new File("src/main/resources/PIM.xlsx");

			testFile.createNewFile();
			FileOutputStream jpegStream = new FileOutputStream(testFile);
			downloader.download(jpegStream);
			jpegStream.close();
			String fileLocation = new File("src/main/resources").getAbsolutePath();
			filePath = testFile.getAbsolutePath();
			new ToCSV().convertExcelToCSV(filePath, fileLocation);
		} catch (DbxException e) {
			log.error("Unable to download file from Dropbox");
			log.error(e.getLocalizedMessage());
		} catch (IOException e) {
			log.error("Unable to download file from Dropbox");
			log.error(e.getLocalizedMessage());
		}

		return filePath;

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
	public Job importUserJob() throws IOException {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
	}

	@Bean
	public Step step1() throws IOException {

		Step firstStep = null;
		firstStep = stepBuilderFactory.get("step1").<Accounts, Accounts> chunk(10).reader(reader())
				.processor(processor()).writer(writer()).build();
		return firstStep;

	}
	// end::jobstep[]
}