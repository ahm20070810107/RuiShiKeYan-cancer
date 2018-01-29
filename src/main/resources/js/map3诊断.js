function(){
    var 使用Count = 0 ;
    if(this.诊断状态 == "是"){
        使用Count = 1;
    }
    var PIDS = []
    if(使用Count){
        PIDS.push(this.PID)
    }
    emit({实体名称:this.标准诊断名_原,实体标本:"",表型名称:this.标准诊断名,标准标本:""},{使用Count:使用Count,PIDS:PIDS,pidSize:PIDS.length,count:1})
}