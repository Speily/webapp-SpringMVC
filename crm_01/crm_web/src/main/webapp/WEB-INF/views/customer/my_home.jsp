<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>凯盛软件CRM-我的客户</title>
    <%@ include file="../base/base-css.jsp"%>
    <style>
        .name-avatar {
            display: inline-block;
            width: 45px;
            height: 45px;
            background-color: #3d5d7c;
            border-radius: 50%;
            text-align: center;
            line-height: 45px;
            font-size: 24px;
            color: #FFF;
        }
        .table>tbody>tr:hover {
            cursor: pointer;
        }
        .table>tbody>tr>td {
            vertical-align: middle;
        }
        .star {
            font-size: 20px;
            color: #a16092;
        }
    </style>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<!-- Site wrapper -->
<div class="wrapper">
    <c:if test="${formWhere =='my'}">
        <jsp:include page="../base/base-side.jsp">
            <jsp:param name="active" value="cust_my"/>
        </jsp:include>
    </c:if>
    <c:if test="${formWhere =='public'}">
        <jsp:include page="../base/base-side.jsp">
            <jsp:param name="active" value="cust_public"/>
        </jsp:include>
    </c:if>
    <!-- 右侧内容部分 -->
    <div class="content-wrapper">
        <!-- Main content -->
        <section class="content">
            <div class="box">

                <div class="box-body">
                    <form class="form-inline">
                        <input type="text" name="keyword" value="${keyword}" class="form-control" placeholder="姓名 或 联系电话">
                        <button class="btn btn-default"><i class="fa fa-search"></i>    搜索</button>
                        <button type="javascript:;" class="btn btn-primary pull-right " id="resetBtn">重置</button>
                    </form>
                </div>
            </div>


            <!-- Default box -->
            <div class="box">
                <div class="box-header with-border">
                    <h3 class="box-title">我的客户</h3>
                    <div class="box-tools pull-right">
                        <input type="hidden" id="formWhere" value="${formWhere}">
                        <a href=" ${formWhere =='my' ? '/customer/my/new' : '/customer/public/new'} " class="btn btn-success btn-sm"><i class="fa fa-plus"></i> 新增客户</a>
                        <a href="/customer/exportExcel" class="btn btn-primary btn-sm"><i class="fa fa-file-excel-o"></i> 导出Excel</a>
                    </div>
                </div>
                <div class="box-body no-padding">

                    <c:if test="${not empty message}">
                        <div class="alert alert-info">${message}</div>
                    </c:if>


                    <table class="table table-hover">
                        <tbody>
                        <tr>
                            <th width="80"></th>
                            <th>姓名</th>
                            <th>职位</th>
                            <th>跟进时间</th>
                            <th>级别</th>
                            <th>联系方式</th>
                        </tr>
                        <c:if test="${empty pageInfo.list}">
                            <tr>
                                <td colspan="6">您还没有任何客户...</td>
                            </tr>
                        </c:if>
                        <c:forEach items="${pageInfo.list}" var="customer">
                            <tr rel="${customer.id}" class="customer_row">
                                <td><span class="name-avatar" style="background-color:${customer.sex == '先生' ? '#808080' : '#FF66CC'};">${fn:substring(customer.custName,0,1)}</span></td>
                                <td>
                                        ${customer.custName}
                                </td>
                                <td>${customer.job}</td>
                                <td><fmt:formatDate value="${customer.lastContactTime}"/></td>
                                <td class="star">${customer.level}</td>
                                <td><i class="fa fa-phone"></i> ${customer.tel} <br></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>

                </div>
                <!-- /.box-body -->
                <c:if test="${pageInfo.pages > 1}" >
                <div class="box-footer">
                    <ul id="pagination-demo" class="pagination-sm pull-right"></ul>
                </div>
                </c:if>
            </div>
            <!-- /.box -->


        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <%@ include file="../base/base-footer.jsp"%>

</div>
<!-- ./wrapper -->

<%@include file="../base/base-js.jsp"%>
<script src="/static/plugins/page/jquery.twbsPagination.min.js"></script>
<script>
    $(function () {

        var formWhere = $("#formWhere").val();

        $("#resetBtn").click(function () {
            window.history.go(-1);
        });

        <c:if test="${pageInfo.pages > 1}" >
            //分页
            $('#pagination-demo').twbsPagination({
                totalPages: ${pageInfo.pages},
                visiblePages: 7,
                first:'首页',
                last:'末页',
                prev:'上一页',
                next:'下一页',
                href:"?p={{number}}"
            });
        </c:if>
        $(".customer_row").click(function () {
            var id = $(this).attr("rel");

            if(formWhere == "my"){

                window.location.href = "/customer/my/"+id;
            }else{
                window.location.href = "/customer/public/"+id;
            }
        });
    });
</script>

</body>
</html>
