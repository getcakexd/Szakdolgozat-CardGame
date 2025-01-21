package hu.benkototh.cardgame.backend.rest;

import hu.benkototh.cardgame.backend.rest.Data.Data;
import hu.benkototh.cardgame.backend.rest.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class HeartBeatRestService {

    @Autowired
    private DataRepository dataRepository;

    @GetMapping("/api/heartbeat")
    public List<Data> all() {
        return dataRepository.findAll();
    }
}
