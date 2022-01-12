package edu.pjatk.app;

import edu.pjatk.app.socials.chat.Conversation;
import edu.pjatk.app.socials.chat.ConversationRepository;
import edu.pjatk.app.socials.chat.Message;
import edu.pjatk.app.task.Task;
import edu.pjatk.app.task.TaskRepository;
import edu.pjatk.app.user.User;
import edu.pjatk.app.user.UserRole;
import edu.pjatk.app.user.profile.Profile;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@RunWith(SpringRunner.class)
@DataJpaTest
class WebAppTests {
    @Autowired
    private TestEntityManager testEntityManager;

    @InjectMocks
    private Task task;

    @Test
    void test_task() {
        task.setId(1l);
        task.setTask_name("test task");
        task.setTask_description("description");
        task.setTask_status("TODO");
        task.setCreation_date(LocalDateTime.now());
        task.setExpiration_date(LocalDateTime.now());

        testEntityManager.persist(task);
        assertThat(testEntityManager.find(Task.class, 1l)).isNotNull();
    }
}
