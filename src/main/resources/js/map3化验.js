function(){
    var 使用Count = 0 ;
    var name = this.化验名称;
    if(name == "" && this.化验组_原 != ""){
        name = this.化验组_原;
    }
    if(this["化验结果定性（新）"] == "阳性" || this["化验结果（定性）新"] == "阳性"){
        使用Count = 1;
    }
    if(name != ""){
        var PIDS = []
        if(使用Count){
            PIDS.push(this.PID)
        }
        emit({实体名称:name,实体标本:this.标准标本_原,表型名称:this.标准化验名,标准标本:this.标准标本},{使用Count:使用Count,PIDS:PIDS,pidSize:PIDS.length,count:1})
    }

}