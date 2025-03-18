package choreographywithdeadline.domain;

import choreographywithdeadline.DeadlineApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Deadline_table")
@Data
//<<< DDD / Aggregate Root
public class Deadline {

    static final int DEADLINE_DURATION = 10 * 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date deadline;

    private Long orderId;

    private Date startedTime;

    public static DeadlineRepository repository() {
        DeadlineRepository deadlineRepository = DeadlineApplication.applicationContext.getBean(
            DeadlineRepository.class
        );
        return deadlineRepository;
    }

    //<<< Clean Arch / Port Method
    public static void schedule(OrderCreated orderCreated) {
       
        Deadline deadline = new Deadline();
        deadline.setOrderId(orderCreated.getId());
        deadline.setStartedTime(new Date(orderCreated.getTimestamp()));
        deadline.setDeadline(new Date(
            deadline.getStartedTime().getTime()+ DEADLINE_DURATION
        ));
        repository().save(deadline);
    }

    public static void sendDeadlineEvents() {
        //1. 모든 데드라인 레코드를 가져온다.
        //2. 현재시간을 구한다.
        //3. 현재시간과 데드라인 필드를 비교해서 시간이 지난 레코드가 있다면...
        //4. 그 레코드를 이용해서 DeadlineReached 이벤트를 만든다.!
        //5. 그 레코드를 삭제한다.
        
        repository().findAll().forEach(deadline ->{
            Date now = new Date();
            if(now.after(deadline.getDeadline())){
                new DeadlineReached(deadline).publishAfterCommit();
                repository().delete(deadline);
            }
        });
        
    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
