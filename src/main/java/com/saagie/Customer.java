package com.saagie;

import java.io.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.Random;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.json.JSONException;
import com.google.api.gax.paging.Page;
import com.google.common.collect.Lists;

// Imports the Google Cloud client library


public class Customer {

    private static    String jsonPath= "/Users/shivakumar/.googlecloud/festive-sunbeam-186117-54e342840689.json";

    private static void listMyBuckets()  {

        try {

            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

            System.out.println("Buckets:");
            Page<Bucket> buckets = storage.list();
            for (Bucket bucket : buckets.iterateAll()) {
                System.out.println(bucket.toString());
            }

        } catch(Exception ex){
            ex.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException, JSONException {

        String filename = "customers.json";
        String bucket = "saagiedemo-customer360";
        String folder = "customer/input/";

       //listMyBuckets();


        if (args.length >= 1) {
            filename = args[0];
        }

        System.out.println("Checking for Environment Specific Information");

        if ( System.getenv("CUSTOMER_BUCKETNAME") == null ){
            System.out.println("CUSTOMER_BUCKETNAME not defined. Using default value "+bucket);
        } else {
            bucket = System.getenv("CUSTOMER_BUCKETNAME");
        }

        if ( System.getenv("CUSTOMER_FOLDER") == null ){
            System.out.println("CUSTOMER_FOLDER not defined. Using default value "+folder);
        }else{
            folder = System.getenv("CUSTOMER_FOLDER");
        }

        Class clazz = Customer.class;
        ClassLoader classLoader = clazz.getClassLoader();
        File inputFile = new File(classLoader.getResource("customers.json").getFile());
        InputStream inputStream = new FileInputStream(inputFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        String key = folder + filename;

        if ( System.getenv("GOOGLE_APPLICATION_CREDENTIALS_STR") == null ){
            System.out.println("GOOGLE_APPLICATION_CREDENTIALS. Exiting...");
            System.exit(1);
        }

        String GOOGLE_APPLICATION_CREDENTIALS_STR = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_STR");
        InputStream credentialsStream = new ByteArrayInputStream(GOOGLE_APPLICATION_CREDENTIALS_STR.getBytes());

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        BlobId blobId = BlobId.of(bucket, key);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();

        System.out.println("Retrieving data from Source and loading into Object Store");

        try (WriteChannel writer = storage.writer(blobInfo)) {
            try {
                writer.write(ByteBuffer.wrap(buffer, 0, buffer.length));
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }


        System.out.println("Completed Loading Data into Object Store. Exiting");
        System.exit(0);


    }

}
