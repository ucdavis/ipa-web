package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.LineItemCommentRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<LineItemComment> copyComments(LineItem originalLineItem, LineItem newLineItem) {
        List<LineItemComment> originalLineItemComments = originalLineItem.getLineItemComments();
        List<LineItemComment> newLineItemComments = newLineItem.getLineItemComments();

        for (LineItemComment originalLineItemComment : originalLineItemComments) {
            LineItemComment newLineItemComment = new LineItemComment();

            newLineItemComment.setLineItem(newLineItem);
            newLineItemComment.setUser(originalLineItemComment.getUser());
            newLineItemComment.setAuthorName(originalLineItemComment.getAuthorName());
            newLineItemComment.setComment(originalLineItemComment.getComment());

            newLineItemComments.add(lineItemCommentRepository.save(newLineItemComment));
        }

        return newLineItemComments;
    }

    @Override
    public List<LineItemComment> findByLineItems(List<LineItem> lineItems) {
        List<LineItemComment> lineItemComments = new ArrayList<>();

        for (LineItem lineItem : lineItems) {
            lineItemComments.addAll(lineItem.getLineItemComments());
        }

        return lineItemComments;
    }
}
