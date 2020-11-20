import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.*;
import java.util.ArrayList;

public class Main {



    public static void main(String[] args) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {

//        String type=args[0];//获取是-c还是-m参数
//        String pathTarget=args[1]; //获取target路径
//        String pathChange=args[2];//获取Changeinfo路径
        String type="-m";
        String pathTarget="E:\\AT2020\\ClassicAutomatedTesting\\4-NextDay\\target";
        String pathChange="E:\\AT2020\\ClassicAutomatedTesting\\4-NextDay\\data\\change_info.txt";
        //将参数打印查看参数是否读取正确
        System.out.println(type);
        System.out.println(pathChange);
        System.out.println(pathTarget);

        Analyze analyze=new Analyze();
        analyze.toDo(type,pathTarget,pathChange);//通过analyze对象来执行主要内容
        System.out.println("done");
        //测试结果与所给的txt是否一样
        //test();

    }

    /**
     * 测试用
     * @throws IOException
     */
   static void  test() throws IOException {
       String path0="E:\\AT2020\\ClassicAutomatedTesting\\0-CMD\\data";
       String path1="E:\\AT2020\\ClassicAutomatedTesting\\1-ALU\\data";
       String path2="E:\\AT2020\\ClassicAutomatedTesting\\2-DataLog\\data";
       String path3="E:\\AT2020\\ClassicAutomatedTesting\\3-BinaryHeap\\data";
       String path4="E:\\AT2020\\ClassicAutomatedTesting\\4-NextDay\\data";
       String path5="E:\\AT2020\\ClassicAutomatedTesting\\5-MoreTriangle\\data";
       String answer_method="\\selection-method.txt";
       String answer_class="\\selection-class.txt";
       String test_method="\\test-selection-method.txt";
       String test_class="\\test-selection-class.txt";
       System.out.println("0-CMD:");
       System.out.println("method:");
       printDiffer(path0+answer_method,path0+test_method);
       System.out.println("class:");
       printDiffer(path0+answer_class,path0+test_class);

       System.out.println("1-ALU:");
       System.out.println("method:");
       printDiffer(path1+answer_method,path1+test_method);
       System.out.println("class:");
       printDiffer(path1+answer_class,path1+test_class);

       System.out.println("2-DataLog:");
       System.out.println("method:");
       printDiffer(path2+answer_method,path2+test_method);
       System.out.println("class:");
       printDiffer(path2+answer_class,path2+test_class);

       System.out.println("3-BinaryHeap:");
       System.out.println("method:");
       printDiffer(path3+answer_method,path3+test_method);
       System.out.println("class:");
       printDiffer(path3+answer_class,path3+test_class);

       System.out.println("4-Next:");
       System.out.println("method:");
       printDiffer(path4+answer_method,path4+test_method);
       System.out.println("class:");
       printDiffer(path4+answer_class,path4+test_class);

       System.out.println("5-MoreTringle:");
       System.out.println("method:");
       printDiffer(path5+answer_method,path5+test_method);
       System.out.println("class:");
       printDiffer(path5+answer_class,path5+test_class);
    }

    /**
     * 打印对比结果
     * @param p1
     * @param p2
     * @throws IOException
     */
    static  void printDiffer(String p1,String p2) throws IOException {

        File file=new File(p1);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line=line=bf.readLine();;
        ArrayList<String> content1=new ArrayList<String>();
        while (line!=null){
            content1.add(line);
            line=bf.readLine();
        }
        File file2=new File(p2);
        BufferedReader bf2 = new BufferedReader(new FileReader(file2));
        String line2=line=bf2.readLine();;
        ArrayList<String> content2=new ArrayList<String>();
        while (line2!=null){
            content2.add(line2);
            line2=bf2.readLine();
        }

        for(String l:content1){
            if(!content2.contains(l) && !l.equals("")){
                System.out.println("缺少："+l);
            }
        }
        for(String l:content2){
            if(!content1.contains(l)){
                System.out.println("多余："+l);
            }
        }
    }
}
