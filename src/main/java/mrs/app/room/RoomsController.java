package mrs.app.room;

import java.time.LocalDate;
import mrs.domain.service.room.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("rooms")
public class RoomsController {

    private RoomService roomService;

    public RoomsController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(Model model) {
        LocalDate today = LocalDate.now();
        return listRooms(today, model);
    }

    @GetMapping(path = "{date}")
    public String listRooms(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable("date") LocalDate date, Model model) {
        var reservedRoomList = roomService.findReservableRoomList();
        model.addAttribute("roomList", reservedRoomList);
        model.addAttribute("date", date);
        return "room/listRooms";
    }
}
