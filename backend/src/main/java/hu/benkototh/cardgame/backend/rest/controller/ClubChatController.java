package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ClubChatController {

    @Autowired
    private IClubMessageRepository clubMessageRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    public List<ClubMessage> getClubChatHistory(long clubId) {
        return clubMessageRepository.findAll().stream()
                .filter(message -> message.getClub().getId() == clubId)
                .toList();
    }

    public ClubMessage sendClubMessage(long clubId, long senderId, String content) {
        Optional<Club> club = clubRepository.findById(clubId);
        Optional<User> user = userRepository.findById(senderId);

        if (club.isEmpty() || user.isEmpty()) {
            return null;
        }

        ClubMessage message = new ClubMessage(club.get(), user.get(), content);
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
}
