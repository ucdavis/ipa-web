package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.CourseCommentRepository;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;

@Service
public class JpaCourseCommentService implements CourseCommentService {
    @Inject CourseCommentRepository courseCommentRepository;
    @Inject UserService userService;
    @Inject CourseService CourseService;
    @Inject Authorization authorization;

    @Override
    public CourseComment create(CourseComment courseCommentDTO) {
        CourseComment courseComment = new CourseComment();

        User user = userService.getOneByLoginId(authorization.getLoginId());
        Course Course = CourseService.getOneById(courseCommentDTO.getCourse().getId());

        courseComment.setCourse(Course);
        courseComment.setUser(user);
        courseComment.setAuthorName(user.getDisplayName());
        courseComment.setComment(courseCommentDTO.getComment());

        courseComment = this.courseCommentRepository.save(courseComment);

        return courseComment;
    }

}
