var listData = null;
var arrayData = new Array();

var mydata = [ {id:"9",invdate:"2007-09-01",name:"test3",note:"<img src=images/check_mark.gif />",amount:"400.00"},{id:"1",invdate:"2007-08-10",name:"test4",note:"<img src=images/check_mark.gif />",amount:"800.00"},{id:"2",invdate:"2008-07-07",name:"test6",note:"<img src=images/check_mark.gif />",amount:"900.00"} ];

function showGrid(listData){ 

	alert(listData);

	$("#list").jqGrid({ 
		data:listData, 
		datatype: "local",  
		colNames:['ID No','Date', 'Name', 'Amount','Status'], 
		colModel:[ 
			{name:'id',index:'id', width:60, sorttype:"int"}, 
			{name:'invdate',index:'invdate', width:90, sorttype:"date"}, 
			{name:'name',index:'name', width:200}, 
			{name:'amount',index:'amount', width:120, align:"right",sorttype:"float"}, 
			{name:'note',index:'note', width:150, sortable:false, align:"center" } 
		],
		multiselect: true, 
		pager: '#pager',
		rowNum:10,
		rowList:[10,20,30],
		sortable:true,
		sortname: 'id',
		sortorder: 'desc',
		caption: "Manipulating Array Data" });  
}


function showMessage(){
	//alert('JavaScript Call');
	
	$.ajax({
        type: "GET",
        url: "/list",
        dataType: "text",
        data: "{}",
        success: function(res) {
            listData = res;
            arrayData = res.split('|');
            //alert(arrayData);
           	showGrid(arrayData);
        }
    });
}


