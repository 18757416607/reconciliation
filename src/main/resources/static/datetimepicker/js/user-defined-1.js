/**
 * Created by Administrator on 2018/1/30.
 */
$(function(){
    $(".selectpicker").selectpicker({
        noneSelectedText : '请选择'//默认显示内容
    });

    //language: 'zh-CN',
    var d1init = new Date(new Date().getTime()-(new Date().getTime()%(24*3600*1000))-(24*3600*1000)-(8*3600*1000));
    var d1 = $("#startDate")
        .val(d1init.getFullYear()+'-'+ (d1init.getMonth()+1) +'-'+d1init.getDate()/*+' 00:00:00'*/);
    $("#startDate").datetimepicker({
        language: 'zh-CN',
        autoclose: true,
        //format: 'yyyy-mm-dd hh:ii:ss',
        format: 'yyyy-mm-dd',
        minView:'month',
//	      	startView:'2',
//	      	viewSelect:'day',
        // initialDate: new Date(),
        // maxView:'4,decade',
        // 1.28 0.0.0
        initialDate:d1init,
        endDate:new Date(new Date().getTime()-(new Date().getTime()%(24*3600*1000*2))-(8*3600*1000)-1),
        todayBtn: true//显示今日按钮
    });
    var d2init = new Date(new Date().getTime()-(new Date().getTime()%(24*3600*1000))-(8*3600*1000)-1);
    var d2 = $("#endDate")
        .val(d2init.getFullYear()+'-'+ (d2init.getMonth()+1) +'-'+d2init.getDate()/*+' 23:59:59'*/)
        .datetimepicker({
            language: 'zh-CN',
            autoclose: true,
            //format: 'yyyy-mm-dd hh:ii:ss',
            format: 'yyyy-mm-dd',
            minView:'month',
//	      	startView:'2',
//	      	viewSelect:'day',
            // initialDate: new Date(),
            // maxView:'4,decade',
            initialDate:d2init,
            endDate:new Date(new Date().getTime()-(new Date().getTime()%(24*3600*1000))-(8*3600*1000)-1),
            todayBtn: true//显示今日按钮
        });

    d1.on('changeDate',function(){
        console.log(d1[0].value);
        d2.datetimepicker('setStartDate', d1[0].value);
    })
});
