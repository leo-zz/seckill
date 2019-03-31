import com.leozz.entity.SecActivity;
import com.leozz.entity.User;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: leo-zz
 * @Date: 2019/3/24 9:11
 */
public class testFunc {


    @Test
    public void testBigDemicRef(){
        //1、不要使用double类型的值创建BigDecimal，尽量使用字符串类型。
        BigDecimal b = new BigDecimal("1.12");
        BigDecimal bd = new BigDecimal(1.12);
        System.out.println(b);
        System.out.println(bd);

        //2、BigDecimal的值是final不可变的，类似String，因此其subtract方法不会对b本身的值造成影响。
        BigDecimal c = b.subtract(new BigDecimal("0.10"));
        System.out.println(b);
        System.out.println(c);

        //3、BigDecimal作为传入参数效果是值传递，不是引用传递。
        BigDecimal d = subTest(b, "0.01");
        System.out.println(b);
        System.out.println(d);
    }

    private BigDecimal  subTest(BigDecimal b,String d){
        return b.subtract(new BigDecimal(d));
    }

    @Test
    //测试Integer到byte类型的转换
    public void testIntegerConvertToByte() {
        Integer i = 100;
        byte a = i.byteValue();
        System.out.println("xxx:" + a);
    }

    @Test
    //entity类初始化后，所有的属性均为null。只有set后对应的属性才有值，没有初始值。
    public void testInitial() {
        SecActivity secActivity = new SecActivity();
        secActivity.setId(123l);
        System.out.println(secActivity);
    }

    @Test
    /**
     * 测试map集合中的对象锁
     * 测试结果：对象锁生效
     t1:99996
     t2:100000
     t3:100000
     */
    public void testMapVlueLock() throws InterruptedException {
        HashMap<String, User> users = new HashMap<>();
        User zhangsan = new User();
        zhangsan.setNickname("zhangsan");
        users.put("zhangsan", zhangsan);

        User lisi = new User();
        lisi.setNickname("lisi");
        users.put("lisi", lisi);
        int count = 100000;

        CountDownLatch countDownLatch = new CountDownLatch(count);

        TestMapVlueLock t1 = new TestMapVlueLock(0);
        TestMapVlueLock t2 = new TestMapVlueLock(0);
        TestMapVlueLock t3 = new TestMapVlueLock(0);


        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                t1.setI(t1.getI() + 1);
                synchronized (zhangsan) {
                    t2.setI(t2.getI() + 1);
                }
                synchronized (this) {
                    t3.setI(t3.getI() + 1);
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        System.out.println("t1:" + t1.getI());
        System.out.println("t2:" + t2.getI());
        System.out.println("t3:" + t3.getI());

    }

    class TestMapVlueLock {
        int i;

        public TestMapVlueLock(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }
    }


}
