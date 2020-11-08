package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.AddressDTO;
import com.leyou.user.UserDTO;
import com.leyou.user.config.PasswordConfig;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.utils.RegexUtils;
import com.sun.xml.internal.ws.encoding.HasEncoding;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Service
public class UserService {

    private static final String KEY_PREFIX = "code:phone:";

    //对密码进行加密
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private UserMapper userMapper;


    /**
     * 数据验证功能
     * @param param
     * @param type
     * @return
     */
    public Boolean checkUserData(String param, Integer type) {

            //校验用户名是否存在

        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(param);
                break;
            case 2:
                 user.setPhone(param);
                 break;
            default:
                 throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        int count = userMapper.selectCount(user);

        return count == 0;
    }

    /**
     * 发送验证码
     * @param phone
     */
    public void sendCode(String phone) {
        //验证手机号码
        if (!RegexUtils.isMobile(phone)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        //随机生成验证码
        String code = RandomStringUtils.randomNumeric(6);

        //将验证码存放在缓存中
        redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);


        //发送RabbitMq消息到ly-sms
        HashMap<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);

        //向RabbitMq中发送消息 参数1为：交换机名称，参数2：路由地址 ，参数3：发送的消息
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME,VERIFY_CODE_KEY,msg);



    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    public void register(User user, String code) {
        //1.校验验证码
        //1.1 从redis 中取出验证码
        String checkCode = (String) redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(checkCode, code)) {
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //2.对密码进行加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //3.存入数据库
        int count = userMapper.insertSelective(user);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        //先验证用户名 是否存在
        User u = new User();
        u.setUsername(username);
        User user = userMapper.selectOne(u);
        System.out.println(user);
        if (user == null) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //2.校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return BeanHelper.copyProperties(user, UserDTO.class);



    }

/*    *//**
     * 根据用户id 和id 查询地址
     * @param
     * @param
     * @return
     *//*
    public AddressDTO queryAddressById(Long userId, Long id) {

        return null;
    }*/





        public static void main(String[] args) {
            Integer[] s = {1, 2, 2, 3, 4, 4, 7, 9, 9};
            findNotSameNumbers(s);
            System.out.println("result = " + findNotSameNumbers(s));
        }


        public static Boolean threeConsecutiveOdds(int[] arr) {
            int flag = 0;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] % 2 == 0) {
                    flag = 0;
                } else if (arr[i] % 2 == 1) {
                    flag++;
                    if (flag == 3) {
                        return true;
                    }
                }
            }
            return false;
        }


    public static Integer[] getArr(int n) {
        Integer[] arr = new Integer[n];
        List<Integer> list = new ArrayList<>();
        int p = 0;
        Random random = new Random();
        for (int i = 0; i < n; i++) {

            int x = 0;
            if (i % 2 == 0) {
                x = random.nextInt(10);
            } else {
                x = -random.nextInt(10);
            }
//            random.nextInt(10);
            p += x;
            list.add(x);
        }
        if (p == 0) {
            return list.toArray(arr);
        } else {
            getArr(n);
        }
        return null;
    }

    private static Integer[] getArr(String s) {
        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        Integer x = random.nextInt(s.length());
        list.add(x);
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (String.valueOf(aChar).equals("I")) {
                x += random.nextInt(s.length());
                list.add(x);
            } else if (String.valueOf(aChar).equals("D")) {
                x -= random.nextInt(s.length());
                list.add(x);
            }
        }
        return list.toArray(new Integer[list.size()]);
    }

    public static int[] deckRevealedIncreasing(int[] deck) {
        int N = deck.length;
        Deque<Integer> index = new LinkedList();
        for (int i = 0; i < N; ++i)
            index.add(i);

        int[] ans = new int[N];
        Arrays.sort(deck);
        for (int card: deck) {
            ans[index.pollFirst()] = card;
            if (!index.isEmpty())
                index.add(index.pollFirst());
        }

        return ans;
    }

    public static int getInt(int[] nums) {
        List<Integer> collect = Arrays.stream(nums).boxed().collect(Collectors.toList());
        Set<Integer> set = new HashSet<>(collect);
        return 1;
    }

    public int maxSubArray(int[] nums) {
        int max = nums[0];
        int former = 0;//用于记录dp[i-1]的值，对于dp[0]而言，其前面的dp[-1]=0
        int cur = nums[0];//用于记录dp[i]的值
        for(int num:nums){
            cur = num;
            if(former>0) cur +=former;
            if(cur>max) max = cur;
            former=cur;
        }
        return max;
    }

    public int findRepeatNumber(int[] nums) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    return nums[i];
                }

            }
        }

        return 0;
    }

    private boolean[][] visited;
    int m, n;
    // 记录四个方向(上右下左),比如第一组是向上移动即 x - 1，而 y 不动，以此类推
    int[][] d = {{-1, 0},{0, 1},{1, 0},{0, -1}};



    public boolean exist(char[][] board, String word) {
        if (board == null || word == null) {
            return false;
        }
        m = board.length;
        n = board[0].length;
        visited = new boolean[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                // 递归搜索
                if (searchWord(board, word, 0, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean searchWord(char[][] board, String word, int index, int x, int y) {
        if (index == word.length() - 1) {
            return board[x][y] == word.charAt(index);
        }
        if (board[x][y] == word.charAt(index)) {
            visited[x][y] = true;
            // 从 4 个方向搜索
            for (int i = 0; i < 4; i++) {
                int newX = x + d[i][0];
                int newY = y + d[i][1];
                // 如果 newX newY 位置合法，且未被访问过，然后递归下去遍历观察
                if (legal(newX, newY) && !visited[newX][newY] &&
                        searchWord(board, word, index + 1, newX, newY)) {
                    return true;
                }
            }
            // 上一步操作完后要恢复原始标记
            visited[x][y] = false;
        }
        return false;
    }

    // 判断新的值 newX newY 是否越界
    private boolean legal(int x, int y) {
        return x >= 0 && x < m && y >= 0 && y < n;
    }


    public List<String> restoreIpAddresses(String s) {
        List<String> ans = new ArrayList<>();
        if (s == null || s.length() == 0) {
            return ans;
        }
        backtrack(s, ans, 0, new ArrayList<>());
        return ans;
    }
    // pos-当前遍历到 s 字符串中的位置，tmp-当前存放已经确定好的 ip 段的数量
    private void backtrack(String s, List<String> ans, int pos, List<String> tmp) {
        if (tmp.size() == 4) {
            // 如果此时 pos 也刚好遍历完整个 s
            if (pos == s.length()) {
                // join 用法：例如 [[255],[255],[111],[35]] -> 255.255.111.35
                ans.add(String.join(".", tmp));
            }
            // 否则直接返回
            return;
        }

        // ip 地址每段最多有三个数字
        for (int i = 1; i <= 3; i++) {
            // 如果当前位置距离 s 末尾小于 3 就不用再分段了，直接跳出循环即可。
            if (pos + i > s.length()) {
                break;
            }

            // 将 s 的子串开始分段
            String segment = s.substring(pos, pos + i);
            int val = Integer.valueOf(segment);
            // 剪枝条件：段的起始位置不能为 0，段拆箱成 int 类型的长度不能大于 255
            if (segment.startsWith("0") && segment.length() > 1 || (i == 3 && val > 255)) {
                continue;
            }

            // 符合要求就加入到 tmp 中
            tmp.add(segment);
            // 继续递归遍历下一个位置
            backtrack(s, ans, pos + i, tmp);
            // 回退到上一个元素，即回溯
            tmp.remove(tmp.size() - 1);
        }
    }

    public static List<Integer> findNotSameNumbers(Integer[] nums) {
        List<Integer> numsList = Arrays.asList(nums);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<Integer>(numsList);

        for (int i = 0; i < nums.length; i++) {

            map.put(nums[i], 0);
        }

        for (int j = 0; j < nums.length; j++) {

            map.put(nums[j], map.get(nums[j]) + 1);
        }
        for (Integer k : set) {
            if (map.get(k) == 1) {
                list.add(k);
            }
        }
        return list;

    }




    public List<String> get(String pathName) throws FileNotFoundException {
        File file=new File(pathName);                  //读取文件
        if(!file.exists()){
            System.out.println("文件不存在");
            return null;
        }
        //获取所有行
        Scanner scanner=new Scanner(file);
        HashMap<String,Integer> hashMap=new HashMap<String,Integer>();
        //遍历
        while(scanner.hasNextLine()) {
            String line=scanner.nextLine();
            //System.out.println(line);
            //根据每个字符去切割
            String[] lineWords=line.split("\\W+");          //导入文章，但是被注释掉了
            Set<String> wordSet=hashMap.keySet();
            for(int i=0;i<lineWords.length;i++) {
                if(wordSet.contains(lineWords[i])) {
                    Integer number=hashMap.get(lineWords[i]);
                    number++;
                    hashMap.put(lineWords[i], number);
                }
                else {
                    hashMap.put(lineWords[i], 1);
                }
            }
        }
        List<String> list = new ArrayList<>();
        int i = 5;
        Map<String, Integer> map = sortAscend(hashMap);
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext() && i > 0) {
            String str = iterator.next();
            list.add(str);
        }
        return list;
    }



    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = (o1.getValue()).compareTo(o2.getValue());
                return compare;
            }
        });

        Map<K, V> returnMap = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }


    public void maxl(int[] a) {


        int temp=1,result=1;
        boolean f=true;
        Arrays.sort(a);//排序
        for(int i=0;i<a.length;i++){
            if(a[i+1]==a[i]+1) temp++;//记录当前连续序列的长度
            else if (a[i+1]==a[i]) continue;//去重
            else {      //当前序列终止，进行下个序列，重新计算
                f=false;
                if(temp>result) result=temp;//更新序列的最大长度
                temp=1;
            }
        }
        if(f) result=temp;//如果只有一个序列，则当前序列的长度就是最大长度
        System.out.println(result);
    }



    /*快排*/
    public static void sort(int arr[],int left,int right){
        if(left<right){
            int mid=partition(arr,left,right);
            sort(arr,left,mid-1);
            sort(arr,mid+1,right);
        }
    }
    public static int partition(int arr[],int left,int right){
        int pointKey=arr[left];
        while(left<right){
            while(arr[right]>pointKey&&left<right){
                right--;
            }
            arr[left]=arr[right];
            while(arr[left]<pointKey&&left<right){
                left++;
            }
            arr[right]=arr[left];
        }
        arr[left]=pointKey;
        return left;

    }
    /*要求在o(n)时间复杂度  用hashSet 空间换时间*/
    public static int find2(int []arr){
        HashSet<Integer> set=new HashSet<>();
        int max=1;
        for(int array:arr){
            set.add(array);
        }
        for(int array:arr){
            if(set.contains(array-1)){//array-1在set里面，直接跳出，开始下一次遍历
                continue;
            }
            else {
                int temp=array;
                while(set.contains(temp)){
                    set.remove(temp);//找到一个就从set中移除一个，这样后续的set在查找时效率会提高
                    temp++;
                }
                //	while(set.remove(temp++));//加分号表示没有循环语句
                if(temp!=array){
                    max=Math.max(max, temp-array);
                    System.out.println("max"+max);
                }
            }
        }
        return max;


    }















}
