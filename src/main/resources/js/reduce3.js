function(key,values){
    var rValues = []
    for(var i = 0 ;i < values.length ; i++){
        var temp =values[i]
        if(Array.isArray(temp)){
            for (var j = 0 ; j < temp.length ; j++){
                rValues.push(temp[j])
            }
        }else{
            rValues.push(temp)
        }
    }
    var result = rValues[0]
    var PIDS = result.PIDS;
    if(rValues.length  > 1){
        for(var i = 1 ;i < rValues.length ; i++){
            var n = rValues[i];
            result.count = result.count + n.count;
            result.使用Count = result.使用Count + n.使用Count;
            for(var j = 0 ;j < n.PIDS.length ; j++){
                var pid = n.PIDS[j];
                if(n.使用Count > 0 && PIDS.indexOf(pid) < 0){
                    PIDS.push(pid)
                    result.pidSize = PIDS.length
                }
            }
        }
    }
    return result;
}