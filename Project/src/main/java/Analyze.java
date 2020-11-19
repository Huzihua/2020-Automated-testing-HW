
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Analyze {
    ArrayList<String> change_method=new ArrayList<String>();//存放所有与修改的地方有关的方法层
    ArrayList<String> change_class=new ArrayList<String>();//存放所有与修改的有关的类层，只有类
    ArrayList<String> change_class_method=new ArrayList<String>();//存放所有与修改的有关的类层的所有方法（要写进txt）
    ArrayList<Method> allMethods=new ArrayList<Method>();//存放所有的方法对象
    //一些必要说明
    String scopePath="scope.txt";
    String exPath="exclusion.txt";
    String source1="\\classes\\net\\mooctest";
    String source2="\\test-classes\\net\\mooctest";
    String outPutPath1="selection-class.txt";
    String outPutPath2="selection-method.txt";

    ArrayList<String> method_relation=new ArrayList<String>();  //存放方法层次的依赖关系，写进dot
    ArrayList<String> class_relation=new ArrayList<String>();   //依赖类层次的依赖关系，写进dot

    /**
     * 主要方法
     * @param type
     * @param pathTarget
     * @param pathChange
     * @throws CancelException
     * @throws ClassHierarchyException
     * @throws InvalidClassFileException
     * @throws IOException
     */
    public void toDo(String type,String pathTarget,String pathChange) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
       //之前调试的时候一次执行全部需要在执行一个任务后进行清空
        change_method.clear();
        change_class.clear();
        change_class_method.clear();
        allMethods.clear();
        method_relation.clear();
        class_relation.clear();

        AnalysisScope scope=loadClass(pathTarget);
        //生成类层次
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        //确定进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        //构建调用图
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);//初始化调用图

        //relation存放所有方法的签名但是没啥用，这个方法通过遍历调用图来构建方法之间的调用关系
        ArrayList<String> relations=getRelation(cg);

        //获取方法依赖图，以生成对应dot文件
        getMethod_relation();
        //获取类依赖图，以生成对应dot文件
        getClass_relation();
        //把依赖关系写进dot
        //writeFile(method_relation,pathTarget.substring(0,pathTarget.length()-6)+"data\\method-CMD-cfa.dot","digraph cmd_method {");
        //writeFile(class_relation,pathTarget.substring(0,pathTarget.length()-6)+"data\\class-CMD-cfa.dot","digraph cmd_class {");

        //找到改变的方法所引起的所有相关方法和类
        doChangeFile(pathChange);

        if(type.equals("-m")) {//打印方法层次
            writeFile2(change_method, outPutPath2);//写进文件
        }else if(type.equals("-c")) {//打印类层次
            writeFile2(change_class_method,outPutPath1);//写进文件
        }

    }

    /**
     * 装载所有的类进去
     * @param path
     * @return
     * @throws IOException
     * @throws InvalidClassFileException
     * @throws ClassHierarchyException
     * @throws CancelException
     */
    public AnalysisScope loadClass(String path) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        //target目录下一共只有两个文件夹有class文件
        String path1=path+source1;
        String path2=path+source2;
        ArrayList<File> allFile = new ArrayList<File>();//存放所有的class文件
        allFile=getClassFile(allFile,path1);
        allFile=getClassFile(allFile,path2);
        //生成类装载器
        ClassLoader classloader = Analyze.class.getClassLoader();
        //生成分析域
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopePath, new File(exPath),classloader);
        for(File f:allFile) {//遍历完所有的class加进分析域
            scope.addClassFileToScope(ClassLoaderReference.Application, f);
        }
        //分析域已经生成，返回分析域
        return scope;
    }

    /**
     * 类层次
     * @param scope
     * @return
     * @throws ClassHierarchyException
     */
    ClassHierarchy getClassLevel(AnalysisScope scope) throws ClassHierarchyException {//生成类层次
        return ClassHierarchyFactory.makeWithRoot(scope);
    }

    /**
     * 遍历一个文件路径下的所有class文件，加进一个Arraylist里
     * @param allFile
     * @param path
     * @return
     */
    ArrayList<File> getClassFile(ArrayList<File> allFile,String path){  //遍历path文件夹下所有类文件并存进allFile中
        File file=new File(path);
        File[] fs = file.listFiles();//目录下所有文件加进数组
        assert fs != null;
        for(File f:fs){//遍历把所有的class文件加进去
            if(f.isFile() && f.getName().endsWith(".class")) {
                assert false;
                allFile.add(f);
            }
        }
        return allFile;//返回结果
    }

    /**
     * 得到所有的method对象，得到它们之间的调用关系
     * @param cg
     * @return
     */
    ArrayList<String> getRelation(CHACallGraph cg){
        //realationships存放所有的调用关系
        ArrayList<String> relationships=new ArrayList<String>(0);
        // 先遍历一遍cg中所有的节点以生成所有的method对象
        for(CGNode node: cg) {
            if(node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    String tempName = classInnerName + " " + signature;
                    relationships.add(tempName);
                    Method tempMethod = new Method(method);//创建该方法的method对象
                    allMethods.add(tempMethod);
                }
            }else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s",node.getMethod(),
                        node.getMethod().getClass()));
            }
        }
//再遍历一遍以获取所有的method之间的调用关系
        for(CGNode node: cg) {
// node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod) {
// node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
// 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
// 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
// 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
// 获取方法签名
                    String signature = method.getSignature();
                    String tempName=classInnerName + " " + signature;
                    Method tempMethod=getMethod(tempName);//找到该方法的节点

                    //获取该method所有调用的节点
                    Iterator<CGNode> succNodes=  cg.getSuccNodes(node);
                    //获取所有调用了该method的节点
                    Iterator<CGNode> preNodes=  cg.getPredNodes(node);
                   while(succNodes.hasNext()){//遍历找到你调用了谁
                       CGNode node2=succNodes.next();
                        if(node2.getMethod() instanceof ShrikeBTMethod) {
                            ShrikeBTMethod temp_method = (ShrikeBTMethod) node2.getMethod();
                            if("Application".equals(temp_method.getDeclaringClass().getClassLoader().toString())) {
                                String classInnerName2 = temp_method.getDeclaringClass().getName().toString();
                                String signature2 = temp_method.getSignature();
                                String tempName2=classInnerName2 + " " + signature2;
                                Method tempMethod2=getMethod(tempName2);//找到该方法的method对象
                                if(!tempMethod.getCallers().contains(tempMethod2)) {
                                    tempMethod.addCaller(tempMethod2);
                                }
                            }
                        }
                    }
                    while(preNodes.hasNext()){//遍历找到谁调用了你，和之前一样
                        CGNode node3=preNodes.next();
                        if(node3.getMethod() instanceof ShrikeBTMethod) {
                            ShrikeBTMethod temp_method = (ShrikeBTMethod) node3.getMethod();
                            if("Application".equals(temp_method.getDeclaringClass().getClassLoader().toString())) {
                                String classInnerName3 = temp_method.getDeclaringClass().getName().toString();
                                String signature3 = temp_method.getSignature();
                                String tempName3=classInnerName3 + " " + signature3;
                                Method tempMethod3=getMethod(tempName3);//找到该方法的节点
                                if(!tempMethod.getCalls().contains(tempMethod3)) {
                                    tempMethod.addCall(tempMethod3);
                                }
                            }
                        }
                    }
                }
           }
        }
        return relationships;//返回所有的调用关系
    }

    /**
     * 处理与改变方法相关事务
     * @param path
     * @throws IOException
     */
    void doChangeFile(String path) throws IOException {
        ArrayList<Method> allCall=new ArrayList<Method>();
        //先读取changeinfo.txt
        File file=new File(path);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line=line=bf.readLine();
        ArrayList<String> changes=new ArrayList<String>();
        while (line!=null){
            changes.add(line);
            line=bf.readLine();
        }
        //遍历一遍所有修改的方法得到所有要修改的方法层次和类层次
        for(String sign:changes){
                    Method method=getMethod(sign);
                    //获取与之相关的方法
                    getAllRealtionships(method);
                   //获取与之相关的类
                   getChange_class(method.getClassInnerName());
                    //把改变的方法的那个对象给去掉
                   change_class.remove(method.getClassInnerName());
               }

        //获取所有相关的类层次，这些类里面的所有方法（也就是得到输出结果selection-class.txt）
        for(Method m:allMethods){
            if(change_class.contains(m.getClassInnerName())){
                //注意去掉初始化函数
                if(!m.getNames().contains("<init>()V")  && !m.getNames().contains("initialize") && !change_class_method.contains(m.getNames())) {
                    change_class_method.add(m.getNames());
                }
            }
        }
           }

    /**
     * 获取所有方法依赖关系，生成dot
     */
    void getMethod_relation(){   //获取整体的所有的方法依赖关系，通过遍历
        for(Method method:allMethods){
            method_relation.addAll(method.getMethod_Relations());
        }
    }

    /**
     * 获取所有的class依赖关系，生成dot
     */
    void getClass_relation(){   //获取整体的所有的类依赖关系，通过遍历
        for(Method method:allMethods){
            ArrayList<String> temp=method.getClass_Relations();
            for(String str:temp){
                if(!class_relation.contains(str)){//防止去重
                    class_relation.add(str);
                }
            }

        }
    }

    /**
     * 通过递归找到一个method对象的所有与之相关的method对象，不会重复，不会死循环
     * @param method
     */
   void getAllRealtionships(Method method){
       if(!change_method.contains(method.getNames())){
           change_method.add(method.getNames());
       }
        for(Method m:method.getCalls()){
            if(!change_method.contains(m.getNames())){
                change_method.add(m.getNames());
                getAllRealtionships(m);
            }

        }
        for(Method m: method.getCalls()){
            if(!change_method.contains(m.getNames())){
                change_method.add(m.getNames());
            }
        }

    }

    /**
     * 写进dot的写法
     * @param content
     * @param path
     * @param firstline
     */
    public  void writeFile(ArrayList<String> content,String path,String firstline) {
        Collections.sort(content);
        try {
            File writeName = new File(path); // 相对路径，如果没有则要建立一个新的txt文件
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(firstline+"\r\n");//dot文件的特殊第一行
                for(String line:content){
                    out.write(line+";\r\n");
                }
                out.write("}\r\n");
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //写找到的与改变相关的方法和类的txt，也就是selection-class/method.txt
    public static void writeFile2(ArrayList<String> content,String path) {
        try {
            File writeName = new File(path); // 相对路径，如果没有则要建立一个新的txt文件
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                for(String line:content){
                    //增加筛选条件，去掉初始化方法，非测试类方法
                    if(line.endsWith("()V") && line.contains("Test") && !line.endsWith("<init>()V")) {
                        out.write(line + "\r\n");
                    }
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过一个method的name来找到与之相关的class，也是通过递归
     * @param className
     */
    public void getChange_class(String className){
        if(!change_class.contains(className)) {
            change_class.add(className);
        }
        for(String str:class_relation){//遍历类依赖关系图来找
            String[] str1=str.split("\"");
            if(str1[1].equals(className)){
                if(!change_class.contains(str1[3])){
                    change_class.add(str1[3]);
                    getChange_class(str1[3]);
                }
                }

            }
    }

    /**
     * 通过一个name来找到唯一对应method对象
     * @param name
     * @return
     */
     Method getMethod(String name){
        for(Method method:allMethods){
            if(method.getNames().equals(name)){
                return method;
            }
        }
        return null;
    }
}
