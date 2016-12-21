/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.loanbroker.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author ptoma
 */
public class LoanRequestSender 
{
    private static final String TASK_QUEUE_NAME = "queue_getCreditScore";
    private static final String REPLY_QUEUE_NAME = "queue_reply_loanRequest";
    private Connection connection;
    private QueueingConsumer consumer;
    private Channel channel;
    
    public LoanRequestSender() throws IOException, TimeoutException
    {
       ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setUsername("student");
        factory.setPassword("cph");
        connection = factory.newConnection();
        channel = connection.createChannel();
        
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        channel.queueDeclare(REPLY_QUEUE_NAME, true, false, false, null);
        
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(REPLY_QUEUE_NAME, true, consumer);
    }
    
    public String call(String message) throws IOException, InterruptedException
    {
        String response = null;
        String corrId = java.util.UUID.randomUUID().toString();
        
        BasicProperties props = new BasicProperties.Builder().correlationId(corrId).build();
        
        channel.basicPublish("", TASK_QUEUE_NAME, props, message.getBytes());
        System.out.println(corrId);
        while(true)
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if(delivery.getProperties().getCorrelationId().equals(corrId))
            {
                response = new String(delivery.getBody());
                System.out.println(response);
                break;
            }
        }
        
        return response;
    }
    /*
    public void sendLoanRequest(String message) throws IOException 
    {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
	factory.setUsername("student");
	factory.setPassword("cph");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        
        channel.basicPublish( "", TASK_QUEUE_NAME, 
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes());
        
        channel.close();
        connection.close();
    }
    */
}
