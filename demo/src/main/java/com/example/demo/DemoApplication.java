package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import es.lanyu.Test;

//@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
//		SpringApplication.run(DemoApplication.class, args);
		ApplicationContext context = 
			    new ClassPathXmlApplicationContext(
			                   new String[]{"configfile1.xml"});

//		Test t = context.getBean(Test.class); //new Test();
		Test t = (Test) context.getBean("test");
		System.out.println(t.getTestString());
	}

	
}
