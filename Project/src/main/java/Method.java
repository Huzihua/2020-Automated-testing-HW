import com.ibm.wala.classLoader.ShrikeBTMethod;

import java.security.AlgorithmConstraints;
import java.util.ArrayList;

public class Method {
    private  String classInnerName;//类内名
    private String signature;//签名
    private String names;//全名
    private ArrayList<Method> callers=new ArrayList<Method>();//所有的你调用了谁
    private ArrayList<Method> calls=new ArrayList<Method>();//所有的谁调用了你
    private ArrayList<Method> allCalls=new ArrayList<Method>();//所有与你有关的方法集合

    /**
     * 构建函数
     * @param method
     */
    public  Method(ShrikeBTMethod method){
        this.setClassInnerName(method.getDeclaringClass().getName().toString());
        this.setSignature(method.getSignature());
        this.setNames(this.getClassInnerName()+" "+this.getSignature());
    }

    /**
     * 获取该方法相关的所有依赖并生成字符串，以生成dot
     * @return
     */
    ArrayList<String> getMethod_Relations(){     //获取所有该方法调用的方法的字符串显示
        ArrayList<String> relations=new ArrayList<String>();
        for(Method m: this.callers){
            relations.add("      \""+m.getSignature()+"\""+" -> "+"\""+this.getSignature()+"\"");
        }
        return relations;
    }

    /**
     * 取该方法相关的所有的类依赖并生成字符串，以生成dot
     * @return
     */
    ArrayList<String> getClass_Relations(){     //获取所有该方法调用的方法的字符串显示
        ArrayList<String> relations=new ArrayList<String>();
        for(Method m: this.calls){
            String temp="      \""+this.getClassInnerName()+"\""+" -> "+"\""+m.getClassInnerName()+"\"";
            if(!relations.contains(temp)) {
                relations.add(temp);
            }
        }
        return relations;
    }

    /**
     * 新增一个谁调用了你
     * @param method
     */
    public void addCall(Method method){
        this.calls.add(method);
    }

    /**
     * 新增一个你调用了谁
     * @param method
     */
    public void addCaller(Method method){
        this.callers.add(method);
    }
    public String getClassInnerName() {
        return classInnerName;
    }

    public void setClassInnerName(String classInnerName) {
        this.classInnerName = classInnerName;
    }
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }
    public ArrayList<Method> getCallers() {
        return callers;
    }

    public void setCallers(ArrayList<Method> callers) {
        this.callers = callers;
    }

    public ArrayList<Method> getCalls() {
        return calls;
    }

    public void setCalls(ArrayList<Method> calls) {
        this.calls = calls;
    }
    public ArrayList<Method> getAllCalls() {
        return allCalls;
    }

    public void setAllCalls(ArrayList<Method> allCalls) {
        this.allCalls = allCalls;
    }




}
