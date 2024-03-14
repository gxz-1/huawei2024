TODO:船只管理逻辑
	停靠：
	根据泊位的运输时间，装货速度，是否有船来选择停靠。
	驶离：
	根据货物是否装满
TODO：机器人装货逻辑
	选择货物：
	选装货点：
	根据有船泊位的位置，装货速度，运输时间来选择泊位，以右下角为装货点就好


/*
             -----------------船只操作-----------------
             */
            for (int i=0;i<5;++i){
                Main.Boat boat=mainInstance.boat[i];//每一帧每艘船有一套相同的操作逻辑
                if (id==1){//第一帧,每一艘船都在虚拟点，前往泊位
                    System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);
                }else if(boat.status==0 || boat.status==2){//非第一帧，船处于0:船移动中 2:泊位外等待
                    //不执行操作
                }else if(boat.status==1){ //非第一帧，船处于等待指令状态
                    if(boat.pos!=-1){//船只已经到达泊位
                        if (id%(1)!=0){//船已经到达泊位且货物尚未装满，可以装货
                            //1可以换成mainInstance.boat_capacity/mainInstance.berth[boat.pos].loading_speed，装货周期=上货点货物量/装货速度，也就是假设港口一直有货物待装的情况下，船装满即走
                            mainInstance.berth[boat.pos].ship=true;//泊位占用信号
                        }else{//船只位于泊位且货物已经装满，可以出发
                            System.out.printf("go %d" + System.lineSeparator(), i);
                        }
                    } else {//船只已经到达虚拟点
                        System.out.printf("ship %d %d" + System.lineSeparator(), i,i*2);

                    }

                }
            }
	


