package com.homo.core.entity.server.entity;

import com.core.ability.base.BaseAbilityEntity;
import com.homo.core.entity.facade.UserEntityFacade;
import com.homo.core.entity.vo.PlayRecord;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.entity.test.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class UserEntity extends BaseAbilityEntity implements UserEntityFacade {
    // lastQueryTime,playRecords这些是用户自定义的业务数据，这些数据由entity自动存储，无需用户操作数据库
    public long lastQueryTime;
    public Map<String, PlayRecord> playRecords = new HashMap<>();
    @Override
    public Homo<QueryInfoResponse> queryInfo(QueryInfoRequest request) {
        String userId = getId();
        long now = System.currentTimeMillis();
        long before = lastQueryTime;
        lastQueryTime= now;
        log.info("login userId {} before {} now {}", userId, before, now);
        QueryInfoResponse success = QueryInfoResponse.newBuilder().setCode(1).setBeforeQueryTime(before).build();
        return Homo.result(success);
    }

    @Override
    public Homo<EnterGameResponse> enterGame(EnterGameRequest request) {
        String userId = getId();
        String playId = request.getPlayId();
        String playType = request.getPlayType();
        String chapterId = request.getChapterId();
        log.info("enterGame userId {} playId {} playType {} chapterId {}", userId, playId, playType, chapterId);
        playRecords.computeIfAbsent(playId,key->{
            PlayRecord record = new PlayRecord();
            record.setPlayId(playId);
            record.setPlayType(playType);
            record.setChapterId(chapterId);
            return record;
        });
        EnterGameResponse success = EnterGameResponse.newBuilder().setCode(1).build();
        return Homo.result(success);
    }

    @Override
    public Homo<LeaveGameResponse> leaveGame(LeaveGameRequest request) {
        String userId = getId();
        String playId = request.getPlayId();
        int isWin = request.getIsWin();
        int score = request.getScore();
        PlayRecord record = playRecords.get(playId);
        if (record == null) {
            log.error("leaveGame userId {} playId {} not found", userId, playId);
            LeaveGameResponse fail = LeaveGameResponse.newBuilder().build();
            return Homo.result(fail);
        }
        record.setIsWin(isWin);
        record.setScore(score);
        log.info("leaveGame userId {} playId {} isWin {} score {}",userId,playId,isWin,score);
        LeaveGameResponse success = LeaveGameResponse.newBuilder().addAllRecords(covertRecord()).build();
        return Homo.result(success);
    }

    private List<PlayRecordPb> covertRecord(){
        return playRecords.values().stream().map(item->{
            PlayRecordPb pb = PlayRecordPb.newBuilder()
                   .setPlayId(item.getPlayId())
                   .setPlayType(item.getPlayType())
                   .setChapterId(item.getChapterId())
                   .build();
            return pb;
        }).collect(Collectors.toList());
    }
}
