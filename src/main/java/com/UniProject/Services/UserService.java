package com.UniProject.Services;

import com.UniProject.DTO.DtoImpl;
import com.UniProject.DTO.TaskDto;
import com.UniProject.DTO.TaskParam;
import com.UniProject.DTO.UserDto;
import com.UniProject.Entities.User;
import com.UniProject.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleToLongFunction;

@Component
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DtoImpl dto;

    private TaskParam taskParam;




    public int saveUser(UserDto userDto){
        try{
            User newUser=userRepository.save(dto.convertUserDtoToUser(userDto));
            if(newUser!=null){
                return 1;//Success
            }
            else{
                return 2;//Failed saving
            }
        }catch (Exception e){
            return 0;//Internal server error
        }
    }
    public TaskDto forTask(String email){
        UserDto user=dto.convertUserToUserDto(userRepository.findByEmail(email));
        //setting the parameter to compute
        taskParam=new TaskParam(user.getAge(),user.getHeight(),
                user.getWeight(),user.getGoal(),
                user.getGender(), user.getActivity_level());

        TaskDto task=new TaskDto();
        task.setTarget_Calorie(computeTargetCalorie(taskParam));
        task.setExercises(ExerciseList(taskParam));

        //need to set here task
        System.out.println(task);
        return task;
    }
    @Transactional
    public void verifyUser(String email){
         userRepository.updateUserEnable(email);
    }
    public UserDto getUser(String email){
        return dto.convertUserToUserDto(userRepository.findByEmail(email));
    }

    public User checkUser(String email,String pass){
        return userRepository.findByEmailAndPassword(email,pass);
    }


    /**
     * For mail
     **/


    public boolean checkForDuplicateEmail(String email){
        User check=userRepository.findByEmail(email);
        return check==null;
    }
    public User findEmail(String email){
        return userRepository.findByEmail(email);
    }
    public boolean checkEmailVerification(String email){
        return userRepository.findByEmail(email).isEnabled();
    }
    public int getVerCode(String toEmail){
        return emailService.sendVerEmail(toEmail);
    }


    /**
     *For Calorie computation
     */
    public double computeTargetCalorie(TaskParam taskParam){
        //calculate bmr
        double a,b,c,d;
        if(taskParam.getGender().equals("male")){
            a=88.362;
            b=13.397;
            c=4.799;
            d=5.677;
        }
        else{
            a=447.593;
            b=9.247;
            c=3.098;
            d=4.330;
        }
        double bmr=a+(b*taskParam.getWeight())+(c*taskParam.getHeight())-(d*taskParam.getAge());
        int goal=0;
        if(taskParam.getGoal().equals("gain") || taskParam.getGoal().equals("muscle")){
            goal+=200;
        }
        else if(taskParam.getGoal().equals("loss")) {
            goal-=200;
        }
        return (bmr*activityMult(taskParam))+goal;
    }
    public double activityMult(TaskParam taskParam){
        if(taskParam.getActivity_level().equals("lazy")){
            return 1.2;
        }
        else if(taskParam.getActivity_level().equals("low")){
            return 1.375;
        }
        else if(taskParam.getActivity_level().equals("moderate")){
            return 1.55;
        }
        else if(taskParam.getActivity_level().equals("high")){
            return 1.725;
        }
        else if(taskParam.getActivity_level().equals("extreme")){
            return 1.9;
        }
        return 0;
    }
   /**/
   public Map<String,Double> ExerciseList(TaskParam taskParam)
   {
       Map<String,Double>exerciseCaloriesMap = new HashMap<>();
       Double Push_up_Cal,Pull_up_Cal,Jumping_jacks_Cal,Squats_Cal,Plank_Cal
               ,Sit_up_Cal,Knee_Pushup_Cal;

       Double height=taskParam.getHeight();
       Double weight=taskParam.getWeight();

       Push_up_Cal= (3.8* weight*(3.0/3600))*3.5;

       Pull_up_Cal=(3.0*weight*(3.0/3600))*3.5;

       Jumping_jacks_Cal=(8.0*weight*(1.5/3600))*3.5;

       Squats_Cal=(3.0*weight*(3.0/3600))*3.5;

       Plank_Cal=(2.0*weight*(45/3600))*3.5;

       Sit_up_Cal=(3.0*weight*(2/3600))*3.5;

       Knee_Pushup_Cal=(3.0*weight*(3.0/3600))*3.5;

       exerciseCaloriesMap.put("Push_up", Push_up_Cal);
       exerciseCaloriesMap.put("Pull_up", Pull_up_Cal);
       exerciseCaloriesMap.put("Jumping_jacks", Jumping_jacks_Cal);
       exerciseCaloriesMap.put("Squats", Squats_Cal);
       exerciseCaloriesMap.put("Plank", Plank_Cal);
       exerciseCaloriesMap.put("Sit_up", Sit_up_Cal);
       exerciseCaloriesMap.put("Knee_Pushup", Knee_Pushup_Cal);



       return exerciseCaloriesMap;
   }

}
