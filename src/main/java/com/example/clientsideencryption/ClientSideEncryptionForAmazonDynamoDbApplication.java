package com.example.clientsideencryption;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.AttributeEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.DynamoDBEncryptor;
import com.amazonaws.services.dynamodbv2.datamodeling.encryption.providers.DirectKmsMaterialProvider;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.AliasListEntry;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@EnableDynamoDBRepositories
public class ClientSideEncryptionForAmazonDynamoDbApplication {

    private final AWSCredentialsProvider credentials;
    private final RegionProvider regionProvider;
    private final String cmkKeyId = "9a06c5ff-a650-42fa-8266-03d361045a9c";

    public ClientSideEncryptionForAmazonDynamoDbApplication(final AWSCredentialsProvider credentials, final RegionProvider regionProvider) {
        this.credentials = credentials;
        this.regionProvider = regionProvider;
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard().withCredentials(credentials)
                .withRegion(regionProvider.getRegion().getName())
                .build();
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        return new DynamoDBMapperConfig.Builder()
                //.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT)
                .build();
    }

    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig dynamoDBMapperConfig) {
        final AWSKMS kms = AWSKMSClientBuilder.standard().withCredentials(credentials)
                .withRegion(regionProvider.getRegion().getName()).build();

        final AliasListEntry cmk = kms.listAliases().getAliases().stream().filter(aliasListEntry -> cmkKeyId.equals(aliasListEntry.getTargetKeyId())).findFirst().get();

        final DirectKmsMaterialProvider cmp = new DirectKmsMaterialProvider(kms, cmk.getAliasArn());
        final DynamoDBEncryptor encryptor = DynamoDBEncryptor.getInstance(cmp);
        return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig, new AttributeEncryptor(encryptor));
    }


    public static void main(String[] args) {
        SpringApplication.run(ClientSideEncryptionForAmazonDynamoDbApplication.class, args);
    }

}
