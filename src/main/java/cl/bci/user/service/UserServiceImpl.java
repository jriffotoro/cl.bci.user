package cl.bci.user.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;
import java.util.Collections;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bci.user.dto.PhoneDTO;
import cl.bci.user.dto.RespuestaJSON;
import cl.bci.user.dto.UserDTO;
import cl.bci.user.dto.LoginDTO;
import cl.bci.user.model.Phone;
import cl.bci.user.model.User;
import cl.bci.user.repository.PhoneRepository;
import cl.bci.user.repository.UserRepository;
import cl.bci.user.exception.BCIException;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private PhoneRepository phoneRepository;

    private MessageSource mensajes;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PhoneRepository phoneRepository, MessageSource mensajes) {
        this.userRepository = userRepository;
        this.phoneRepository = phoneRepository;
        this.mensajes = mensajes;
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public RespuestaJSON login(LoginDTO loginDTO) {
        return new RespuestaJSON(RespuestaJSON.EstadoType.OK.getRespuestaJSONS(), mensajes.getMessage("ok", null, LocaleContextHolder.getLocale()), loginDTO);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public RespuestaJSON crearUser(UserDTO user) throws BCIException {
        Optional<User> up = userRepository.email(user.getEmail());
        if (up.isPresent()) {
            throw new BCIException(mensajes.getMessage("user.correo-ya-registrado", null, LocaleContextHolder.getLocale()));
        }
        User u = new User();
        BeanUtils.copyProperties(user, u);
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        u.setId(uuidAsString);
        u.setCreated(new Timestamp(new Date().getTime()));
        u.setLast_login(new Timestamp(new Date().getTime()));
        u.setIsactive(true);         
        userRepository.save(u);
        List<PhoneDTO> p = user.getPhones();
        if (p!=null) {
            p.forEach((final PhoneDTO phone) -> { 
                Phone f = new Phone();
                BeanUtils.copyProperties(phone, f);
                f.setUser(u); 
                phoneRepository.save(f); 
            });
        }
        user.setCreated(u.getCreated());
        user.setLast_login(u.getLast_login());
        user.setIsactive(u.getIsactive());
        return new RespuestaJSON(RespuestaJSON.EstadoType.OK.getRespuestaJSONS(), mensajes.getMessage("user.creado", null, LocaleContextHolder.getLocale()), u);
    }

    @Override
    public RespuestaJSON getUsers() {
        List<UserDTO> tDto = new ArrayList<UserDTO>();
        List<User> t = userRepository.findAll();
        if (t!=null) {
            t.forEach((final User user) -> {
                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(user, userDTO);
                if (user.getPhones()!=null) {
                    userDTO.setPhones(user.getPhones().stream().filter(Objects::nonNull).map((Phone phone) -> {
                        PhoneDTO phoneDTO = new PhoneDTO();
                        BeanUtils.copyProperties(phone, phoneDTO);
                        return phoneDTO;
                    }).collect(Collectors.toList()));
                }
                tDto.add(userDTO);
            });
        }
        return new RespuestaJSON(RespuestaJSON.EstadoType.OK.getRespuestaJSONS(), mensajes.getMessage("ok", null, LocaleContextHolder.getLocale()), tDto);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public RespuestaJSON borraUser(String id) {
        Optional<User> t = userRepository.findById(id);
        if (t.isPresent()) {
            User u = t.get();
            UserDTO user = new UserDTO();
            BeanUtils.copyProperties(u, user);
            List<Phone> p = u.getPhones();
            p.forEach((final Phone phone) -> phoneRepository.delete(phone));  
            userRepository.delete(u);
            return new RespuestaJSON(RespuestaJSON.EstadoType.OK.getRespuestaJSONS(), mensajes.getMessage("user.eliminado", null, LocaleContextHolder.getLocale()), user);
        }   
        return new RespuestaJSON(RespuestaJSON.EstadoType.ERROR.getRespuestaJSONS(), mensajes.getMessage("user.no-encontrado", null, LocaleContextHolder.getLocale()));       
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public RespuestaJSON actualizaUser(UserDTO user) {
        Optional<User> t = userRepository.findById(user.getId());
        if (t.isPresent()) {
            User u = t.get();
            u.setEmail(user.getEmail()!=null ? user.getEmail() : u.getEmail());
            u.setIsactive(user.getIsactive()!=null ? user.getIsactive() : u.getIsactive());
            u.setLast_login(new Timestamp(new Date().getTime()));
            u.setModified(new Timestamp(new Date().getTime()));
            u.setName(user.getName());
            u.setPassword(user.getPassword());
            return new RespuestaJSON(RespuestaJSON.EstadoType.OK.getRespuestaJSONS(), mensajes.getMessage("user.actualizado", null, LocaleContextHolder.getLocale()), user);
        }   
        return new RespuestaJSON(RespuestaJSON.EstadoType.ERROR.getRespuestaJSONS(), mensajes.getMessage("user.no_encontrado", null, LocaleContextHolder.getLocale()));       
    }
    
}