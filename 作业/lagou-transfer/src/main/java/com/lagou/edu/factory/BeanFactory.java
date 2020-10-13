package com.lagou.edu.factory;

import com.alibaba.druid.util.StringUtils;
import com.lagou.edu.myAnnotation.Autowired;
import com.lagou.edu.myAnnotation.Service;
import com.lagou.edu.myAnnotation.Transactional;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.reflections.Reflections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 应癫
 *
 * 工厂类，生产对象（使用反射技术）
 */
//通过监听来加载容器
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象


//    static {
//        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
//        // 加载xml
//        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
//        // 解析xml
//        SAXReader saxReader = new SAXReader();
//        try {
//            Document document = saxReader.read(resourceAsStream);
//            Element rootElement = document.getRootElement();
//            List<Element> beanList = rootElement.selectNodes("//bean");
//            for (int i = 0; i < beanList.size(); i++) {
//                Element element =  beanList.get(i);
//                // 处理每个bean元素，获取到该元素的id 和 class 属性
//                String id = element.attributeValue("id");        // accountDao
//                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
//                // 通过反射技术实例化对象
//                Class<?> aClass = Class.forName(clazz);
//                Object o = aClass.newInstance();  // 实例化之后的对象
//
//                // 存储到map中待用
//                map.put(id,o);
//
//            }
//
//            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
//            // 有property子元素的bean就有传值需求
//            List<Element> propertyList = rootElement.selectNodes("//property");
//            // 解析property，获取父元素
//            for (int i = 0; i < propertyList.size(); i++) {
//                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
//                String name = element.attributeValue("name");
//                String ref = element.attributeValue("ref");
//
//                // 找到当前需要被处理依赖关系的bean
//                Element parent = element.getParent();
//
//                // 调用父元素对象的反射功能
//                String parentId = parent.attributeValue("id");
//                Object parentObject = map.get(parentId);
//                // 遍历父对象中的所有方法，找到"set" + name
//                Method[] methods = parentObject.getClass().getMethods();
//                for (int j = 0; j < methods.length; j++) {
//                    Method method = methods[j];
//                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
//                        method.invoke(parentObject,map.get(ref));
//                    }
//                }
//
//                // 把处理之后的parentObject重新放到map中
//                map.put(parentId,parentObject);
//
//            }
//
//
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//
//    }


    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }


    public static void initialized() {
        try{
            //扫描获取反射对象集合
            Reflections reflections = new Reflections("com.lagou.edu");
            Set<Class<?>> servecesTypesAnnotatedWith = reflections.getTypesAnnotatedWith(Service.class);
            for (Class<?> c : servecesTypesAnnotatedWith) {
                // 通过反射技术实例化对象
                Object bean = c.newInstance();
                Service annotation = c.getAnnotation(Service.class);

                //对象ID在service注解有value时用value，没有时用类名
                if(StringUtils.isEmpty(annotation.value())){
                    //由于getName获取的是全限定类名，所以要分割去掉前面包名部分
                    String[] names = c.getName().split("\\.");
                    BeanFactory.map.put(names[names.length-1], bean);
                }else{
                    map.put(annotation.value(), bean);
                }
            }
            // 实例化完成之后维护对象的依赖关系Autowired，检查哪些对象需要传值进入，
            for(Map.Entry<String, Object> a: map.entrySet()){
                Object o = a.getValue();
                Class c = o.getClass();
                //获取所有的变量
                Field[] fields = c.getDeclaredFields();
                //遍历属性，若持有Autowired注解则注入
                for (Field field : fields) {
                    //判断是否是使用注解的参数
                    Annotation[] s = field.getAnnotations();
                    if (field.isAnnotationPresent(Autowired.class)) {
                        String[] names = field.getType().getName().split("\\.");
                        String name = names[names.length-1];
                        //Autowired注解的位置需要set方法，方便c.getMethods()获取
                        Method[] methods = c.getMethods();
                        for (int j = 0; j < methods.length; j++) {
                            Method method = methods[j];
                            if(method.getName().equalsIgnoreCase("set" + name)) {
                                method.invoke(o,map.get(name));
                            }
                        }
                    }
                }
                //判断对象类是否持有Transactional注解，若有则修改对象为代理对象
                if(c.isAnnotationPresent(Transactional.class)){
                    //获取代理工厂
                    ProxyFactory proxyFactory = (ProxyFactory) BeanFactory.getBean("ProxyFactory");
//                    ProxyFactory proxyFactory = new ProxyFactory();
                    Class[] face = c.getInterfaces();//获取类c实现的所有接口
                    //判断对象是否实现接口
                    if(face!=null&&face.length>0){
                        //实现使用JDK
                        o = proxyFactory.getJdkProxy(o);
                    }else{
                        //没实现使用CGLIB
                        o = proxyFactory.getCglibProxy(o);
                    }
                }

                // 把处理之后的object重新放到map中
                map.put(a.getKey(),o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
