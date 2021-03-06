package com.kaishengit.crm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kaishengit.crm.entity.Customer;
import com.kaishengit.crm.entity.CustomerExample;
import com.kaishengit.crm.entity.User;
import com.kaishengit.crm.mapper.CustomerMapper;
import com.kaishengit.crm.service.CustomerService;
import com.kaishengit.exception.ServiceException;
import com.kaishengit.weixin.WeiXinUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private WeiXinUtil weiXinUtil;

    /**
     * 从配置文件中读取trade
     */
    @Value("#{'${cust.trade}'.split(',')}")
    private List<String> tradeList;

    /**
     * 从配置文件中读取trade
     */
    @Value("#{'${cust.source}'.split(',')}")
    private List<String> sourceList;

    /**
     * 新增个人客户
     *
     * @param user
     * @param cust
     */
    @Override
    @Transactional
    public void newMyCust(User user, Customer cust) {
        Integer userId = user.getId();
        cust.setUserId(userId);
        cust.setCreateTime(new Date());
        customerMapper.insert(cust);

        //发微信消息给部门
        //weiXinUtil.sendTextMessageToDept("(群消息)新建个人客户："+cust.getCustName()+",","1");

        //发微信消息给个人
        weiXinUtil.sendTextMessageToUser("(个人消息)新建个人客户："+cust.getCustName()+",","ShiLei");
    }
    /**
     * 新增公海客户
     *
     * @param user
     * @param cust
     */
    @Override
    @Transactional
    public void newPubCust(User user, Customer cust) {
        Integer userId = user.getId();
        cust.setMark("该客户由"+ user.getUserName()+"创建");
        cust.setCreateTime(new Date());
        cust.setUserId(0);
        customerMapper.insert(cust);
        //发微信消息给管理员
        weiXinUtil.sendTextMessageToDept("新建公海客户："+cust.getCustName()+",","ShiLei");
    }

    /**
     * 分页查询客户
     *
     * @param queryParam 查询条件Map集合
     * @param user       用户
     * @return PageInfo<Customer>
     */
    @Override
    public PageInfo<Customer> findMyCustList(Map<String, Object> queryParam, User user) {

        Integer pageNo = (Integer) queryParam.get("pageNo");
        String keyword = (String) queryParam.get("keyword");

        if (StringUtils.isNotBlank(keyword)) {
            keyword = "%" + keyword + "%";
        }
        PageHelper.startPage(pageNo, 8);

        List<Customer> customerList = customerMapper.selectByParam(user, keyword);

        PageInfo<Customer> pageInfo = new PageInfo<>(customerList);

        return pageInfo;
    }

    /**
     * 获取行业集合
     *
     * @return List<String>
     */
    @Override
    public List<String> findTradeList() {
        return tradeList;
    }

    /**
     * 获取客户来源集合
     *
     * @return List<String>
     */
    @Override
    public List<String> findSourceList() {
        return sourceList;
    }

    /**
     * 根据id查询客户
     *
     * @param id
     * @return Customer
     */
    @Override
    public Customer findById(Integer id) {
        return customerMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新客户信息
     *
     * @param customer 客户
     */
    @Override
    public void update(Customer customer) {
        customer.setUpdateTime(new Date());
        customerMapper.updateByPrimaryKeySelective(customer);
    }

    /**
     * 删除客户
     *
     * @param id 客户id
     */
    @Override
    public void del(Integer id) {
        // TODU 删除客户相关项
        customerMapper.deleteByPrimaryKey(id);
    }

    /**
     * 放入公海
     *
     * @param customer
     */
    @Override
    public void toPublic(Customer customer) {

        customer.setMark("来自于——" + customer.getCustName());
        customer.setLastContactTime(new Date());
        customer.setUserId(0);

        customerMapper.updateByPrimaryKeySelective(customer);
    }

    /**
     * 转交他人
     *
     * @param customer
     * @param userId
     */
    @Override
    public void turnToSomeone(Customer customer, Integer userId, User user) {
        customer.setMark("来自于——" + user.getUserName());
        customer.setUserId(userId);
        customerMapper.updateByPrimaryKeySelective(customer);
    }

    /**
     * 客户信息导出excet
     */
    @Override
    public void exportExcel(User user, OutputStream outputStream) {

        CustomerExample customerExample = new CustomerExample();
        customerExample.createCriteria().andUserIdEqualTo(user.getId());
        List<Customer> customerList = customerMapper.selectByExample(customerExample);

        //承自Control层

        //3.创建工作表
        Workbook workbook = new HSSFWorkbook();
        //4.创建sheet页
        Sheet sheet = workbook.createSheet(user.getUserName() + "的客户资料");
        //5.创建数据
        Row row = sheet.createRow(0);//行
        Cell cell = row.createCell(0);//单元格
        cell.setCellValue("客户名称");//值

        row.createCell(1).setCellValue("职位");
        row.createCell(2).setCellValue("级别");
        row.createCell(3).setCellValue("地址");
        row.createCell(4).setCellValue("联系电话");

        for (int i = 0; i < customerList.size(); i++) {
            Customer customer = customerList.get(i);
            Row dataRow = sheet.createRow(i + 1);
            dataRow.createCell(0).setCellValue(customer.getCustName());
            dataRow.createCell(1).setCellValue(customer.getJob());
            dataRow.createCell(2).setCellValue(customer.getLevel());
            dataRow.createCell(3).setCellValue(customer.getAddress());
            dataRow.createCell(4).setCellValue(customer.getTel());
        }
        //6.写入磁盘

        try {
            workbook.write(outputStream);
        } catch (Exception ex) {
            throw new ServiceException("导出Excel异常", ex);
        }

    }

    /**
     * 查询我的客户
     * @param user
     * @return
     */
    @Override
    public List<Customer> findCustList(User user) {
        CustomerExample customerExample = new CustomerExample();
        customerExample.createCriteria().andUserIdEqualTo(user.getId());
        return customerMapper.selectByExample(customerExample);
    }

    @Override
    public void updateLastConcatTime(Customer customer,Date date) {
        CustomerExample customerExample = new CustomerExample();
        customerExample.createCriteria().andLastContactTimeEqualTo(date);
        customerMapper.updateByExampleSelective(customer,customerExample);
    }

    /**
     * 获取各种星级客户数量
     * @return
     */
    @Override
    public List<Map<String, Object>> findCustomerLevelCount() {
        return customerMapper.countCustLevel();
    }

}
