package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.DAO.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.interfaces.ItemRequestService;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.interfaces.UserService;
import ru.practicum.shareit.utils.PaginationServiceClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {


    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final ItemServiceImpl itemService;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestDto itemRequestDto, Long userId) {
        var userDto = userService.getUserById(userId);
        var itemRequest = itemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(userMapper.toUser(userDto));
        var createdItemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toItemRequestDto(createdItemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByUser(Long userId) {
        userService.getUserById(userId);
        var itemRequests = itemRequestRepository.findAllRequestsByRequesterIdOrderByCreatedDesc(userId);
        return getAllRequestsWithItems(itemRequests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);
        Pageable page = PaginationServiceClass.pagination(from, size);
        var itemRequests = itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId, page);
        return getAllRequestsWithItems(itemRequests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);
        var itemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос с id = " + requestId + " отсутствует."));
        var itemRequestDto = itemRequestMapper.toItemRequestDto(itemRequest);
        var itemsDto = itemService.findAllItemsByRequestId(requestId);
        itemRequestDto.setItems(itemsDto);
        return itemRequestDto;
    }

    private List<ItemRequestDto> getAllRequestsWithItems(List<ItemRequest> itemRequests) {
        var itemReqIds = itemRequests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        var mapRequestsItems = itemService.findAllItemsByRequestIds(itemReqIds);
        List<ItemRequestDto> itemRequestDtos = new ArrayList<>();

        for (ItemRequest itemRequest : itemRequests) {
            var items = mapRequestsItems.get(itemRequest);
            if (items != null) {
                var req = itemRequestMapper.toItemRequestDto(itemRequest);
                req.setItems(itemMapper.toListItemDto(items));
                itemRequestDtos.add(req);
            } else {
                itemRequest.setItems(new ArrayList<>());
                itemRequestDtos.add(itemRequestMapper.toItemRequestDto(itemRequest));
            }
        }
        return itemRequestDtos;
    }
}
