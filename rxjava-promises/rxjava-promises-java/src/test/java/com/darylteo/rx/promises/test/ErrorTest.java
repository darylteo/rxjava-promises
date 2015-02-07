package com.darylteo.rx.promises.test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.darylteo.rx.promises.java.Promise;
import com.darylteo.rx.promises.java.functions.PromiseAction;
import com.darylteo.rx.promises.java.functions.PromiseFunction;


public class ErrorTest {
	
	private CountDownLatch lock;
	private boolean handled;
	private boolean fufilled; 
	
	@Before
	public void setupLock(){
		lock = new CountDownLatch(1);
		handled = false;
		fufilled = false;
	}
	
	@Test
	public void errorsShouldBeHandled() throws InterruptedException{
		Promise<String> initialPromise = new Promise<String>();
		
		initialPromise
		.then(new ErrorThrowingClass())
		.then(new PromiseAction<Integer>() {
			@Override
			public void call(Integer t1) {
				fufilled = true;
				lock.countDown();
			}
		}, new PromiseAction<Exception>() {
			@Override
			public void call(Exception t1) {
				handled = true;
				lock.countDown();
			}
		});
		
		initialPromise.fulfill("Hello");
		lock.await(200, TimeUnit.MILLISECONDS);
		assertFalse("Should not be fufilled", fufilled);
		assertTrue("Error should be handled", handled);
	}
	
	@Test
	public void exceptionsShouldBeHandled() throws InterruptedException{
		Promise<String> initialPromise = new Promise<String>();
		
		initialPromise
		.then(new ExceptionThrowingClass())
		.then(new PromiseAction<Integer>() {
			@Override
			public void call(Integer t1) {
				fufilled = true;
				lock.countDown();
			}
		}, new PromiseAction<Exception>() {
			@Override
			public void call(Exception t1) {
				handled = true;
				lock.countDown();
			}
		});
		
		initialPromise.fulfill("Hello");
		lock.await(200, TimeUnit.MILLISECONDS);
		assertFalse("Should not be fufilled", fufilled);
		assertTrue("Exception should be handled", handled);
	}
	
	@Test
	public void errorsShouldBeHandledImmediately() throws InterruptedException {
		Promise<String> initialPromise = new Promise<String>();
		
		initialPromise
		.then(new ErrorThrowingClass(), new PromiseAction<Exception>() {
			@Override
			public void call(Exception t1) {
				handled = true;
				lock.countDown();
			}
		});
		
		initialPromise.fulfill("Hello");
		lock.await(200, TimeUnit.MILLISECONDS);
		assertFalse("Should not be fufilled", fufilled);
		assertTrue("Error should be handled", handled);
	}
	
	public static class ErrorThrowingClass implements PromiseFunction<String, Integer> {
		@Override
		public Integer call(String t1) {
			throw new AssertionError("Should be caught");
		}
	}
	
	public static class ExceptionThrowingClass implements PromiseFunction<String, Integer> {
		@Override
		public Integer call(String t1) {
			throw new IllegalStateException("Should be caught");
		}
	}
}
