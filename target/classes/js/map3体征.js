//function(){
//    var name = this.部位1 + this.体征 + this.体征定性描述 + this.体征定量描述 + this.体征定量单位
//    var 使用Count = 0 ;
//    if(this.体征 != "" && this.否定词 == ""){
//        使用Count = 1;
//    }
//    var PIDS = []
//    if(使用Count){
//        PIDS.push(this.PID)
//    }
//    if(this.体征 != "")emit({实体名称:name,实体标本:"",表型名称:name,标准标本:""},{使用Count:使用Count,PIDS:PIDS,pidSize:PIDS.length,count:name==""?0:1})
//}

function(){
    var name = this.部位1 +this.否定词+ this.体征 + this.体征定性描述 + this.体征定量描述 + this.体征定量单位
    var 使用Count = 0 ;
    if(this.体征 != "" || this.体征定性描述 != ""){
        使用Count = 1;
    }
    var PIDS = []
    if(使用Count){
        PIDS.push(this.PID)
    }
    if(this.体征 != "" || this.体征定性描述 != "")emit({实体名称:name,实体标本:"",表型名称:name,标准标本:""},{使用Count:使用Count,PIDS:PIDS,pidSize:PIDS.length,count:name==""?0:1})
}