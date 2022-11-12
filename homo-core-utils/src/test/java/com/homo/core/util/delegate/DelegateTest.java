package com.homo.core.util.delegate;

import com.homo.core.utils.delegate.*;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class DelegateTest {
    public Publisher publisher = new Publisher();
    public TestCall testCall = new TestCall();
    @FunctionalInterface
    interface MyDelegate{
        Integer call(String param);
    }
    class Publisher{
        public DelegateVoid delegateVoid = new DelegateVoid();
        public Delegate1PR<String,Integer> delegate1PR = new Delegate1PR<>();
        public Delegate1PVoid<Integer> delegate1PVoid = new Delegate1PVoid<>();
        public DelegateInterface<MyDelegate> delegateInterface = new DelegateInterface<>(MyDelegate.class);
    }

    @Test
    public void testDelegateVoid(){
        DelegateVoid.ExecuteFun executeFun = () -> System.out.println("delegateVoid bindToHead 1");
        DelegateVoid.ExecuteFun executeFun2 = () -> System.out.println("delegateVoid bindToHead 2");
        DelegateVoid.ExecuteFun executeFun3 = () -> System.out.println("delegateVoid bindToTail 1");
        DelegateVoid.ExecuteFun subSubExecuteFun = () -> System.out.println("delegateVoid bindToTail subSubExecuteFun 1");
        DelegateVoid.ExecuteFun subExecuteFun = () -> {
            System.out.println("delegateVoid bindToTail subExecuteFun 1");
            publisher.delegateVoid.bindToTail(subSubExecuteFun);
        };

        publisher.delegateVoid.bindToHead(executeFun);
        publisher.delegateVoid.bindToHead(executeFun2);
        publisher.delegateVoid.bindToTail(executeFun3);
        publisher.delegateVoid.bindToTail(subExecuteFun);
        System.out.println("第一次publish");
        publisher.delegateVoid.publish();
        publisher.delegateVoid.unbind(executeFun);
        System.out.println("第二次publish");
        publisher.delegateVoid.publish();
    }
    interface Test1{
        Integer ret(String param);
    }

    interface Test2{
        Integer ret(String param, Integer p2);
    }

    interface Test3{
        Integer ret(String...param);
    }


    class TestCall{
        Integer p1(String aaa){
            System.out.println("TestCall p1:" + aaa);
            return 1;
        }
        Integer p2(String bbb, Integer p2){
            System.out.println("TestCall p2:" + bbb + ":" + p2);
            return p2;
        }

        Integer p3(String...strings){
            if (strings != null){
                System.out.println("TestCall p3:" + strings.length);
                return strings.length;
            }
            System.out.println("TestCall p3:" + 0);
            return 0;
        }

    }
    @Test
    public void testDelegate1PR(){
        Delegate1PR.ExecuteFun<String,Integer> executeFun1 = new Delegate1PR.ExecuteFun<String, Integer>() {
            @Override
            public Integer apply(String s) throws Exception {
                System.out.println("executeFun1 param "+ s);
                return 1;
            }
        };
        Delegate1PR.ExecuteFun<String,Integer> executeFun2 = new Delegate1PR.ExecuteFun<String, Integer>() {
            @Override
            public Integer apply(String s) throws Exception {
                System.out.println("executeFun2 param "+ s);
                return 2;
            }
        };
        Function<String,Integer> executeFun3 = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                System.out.println("executeFun3 param "+ s);
                return 3;
            }
        };

        publisher.delegate1PR.bindToHead(executeFun1);
        publisher.delegate1PR.bindToHead(executeFun1);
        publisher.delegate1PR.bindToTail(executeFun2);
        publisher.delegate1PR.bindToTail(executeFun2);
        publisher.delegate1PR.bindToHead(executeFun3::apply);//注意，
        publisher.delegate1PR.bindToHead(executeFun3::apply);
        publisher.delegate1PR.bindToHead(testCall::p1);
        publisher.delegate1PR.bindToHead(testCall::p1);
        Integer rel = publisher.delegate1PR.publish("aaa");
        System.out.println("delegate1PR rel "+rel);
    }

    @Test
    public void testDelegateInterface(){
        Test1 test1 = new Test1() {
            @Override
            public Integer ret(String param) {
                System.out.println("Test1 param "+param);
                return 2;
            }
        };
        publisher.delegateInterface.bindToHead(test1::ret);
        publisher.delegateInterface.bindToHead(test1::ret);
        publisher.delegateInterface.bindToTail(testCall::p1);
        publisher.delegateInterface.bindToTail(testCall::p1);
        Integer rel = publisher.delegateInterface.asInterface().call("bbb");
        System.out.println("testDelegateInterface call rel " +rel);
    }
}
