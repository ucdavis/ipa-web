package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.LineItemCommentRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class JpaLineItemCommentService implements LineItemCommentService {
    @Inject LineItemCommentRepository lineItemCommentRepository;
    @Inject UserService userService;
    @Inject LineItemService lineItemService;

    @Override
    public LineItemComment create(LineItemComment lineItemCommentDTO) {
        LineItemComment lineItemComment = new LineItemComment();

        User user = userService.getOneById(lineItemCommentDTO.getUser().getId());
        LineItem lineItem = lineItemService.findById(lineItemCommentDTO.getLineItem().getId());

        lineItemComment.setLineItem(lineItem);
        lineItemComment.setUser(user);
        lineItemComment.setAuthorName(user.getDisplayName());
        lineItemComment.setComment(lineItemCommentDTO.getComment());

        lineItemComment = this.lineItemCommentRepository.save(lineItemComment);

        return lineItemComment;
    }
}
