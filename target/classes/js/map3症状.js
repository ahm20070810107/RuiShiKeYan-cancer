function(){
    var name = this.部位1 + this.症状1
    var 使用Count = 0 ;
    if(this.症状1 != "" && this.否定词 == ""){
          使用Count = 1;
     }
    var PIDS = []
    if(使用Count){
        PIDS.push(this.PID)
    }
    if(this.症状1 != "")emit({实体名称:name,实体标本:"",表型名称:name,标准标本:""},{使用Count:使用Count,PIDS:PIDS,pidSize:PIDS.length,count:name==""?0:1})
}