package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ClubChatController {

    @Autowired
    private IClubMessageRepository clubMessageRepository;

    @Lazy
    @Autowired
    private UserController userController;
    
    @Lazy
    @Autowired
    private ClubController clubController;

    public List<ClubMessage> getClubChatHistory(long clubId) {
        return clubMessageRepository.findAll().stream()
                .filter(message -> message.getClub().getId() == clubId)
                .toList();
    }

    public ClubMessage sendClubMessage(long clubId, long senderId, String content) {
        User user = userController.getUser(senderId);
        Club club = clubController.getClub(clubId);

        if (club == null || user == null) {
            return null;
        }

        ClubMessage message = new ClubMessage(club, user, content);
        return clubMessageRepository.save(message);
    }

    public ClubMessage unsendClubMessage(long messageId) {
        Optional<ClubMessage> message = clubMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return null;
        }

        message.get().setStatus("unsent");
        message.get().setContent("This message has been unsent.");
        return clubMessageRepository.save(message.get());
    }

    public ClubMessage removeClubMessage(long messageId) {
        Optional<ClubMessage> message = clubMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return null;
        }

        message.get().setStatus("removed");
        message.get().setContent("This message has been removed by a moderator.");
        return clubMessageRepository.save(message.get());
    }
    
    public void deleteMessagesByUser(User user) {
        List<ClubMessage> messages = clubMessageRepository.findAll().stream()
                .filter(clubMessage -> clubMessage.getSender().getId() == user.getId())
                .toList();
        
        clubMessageRepository.deleteAll(messages);
    }
}
