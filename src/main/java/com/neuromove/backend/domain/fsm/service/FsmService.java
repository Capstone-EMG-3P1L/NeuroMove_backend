package com.neuromove.backend.domain.fsm.service;

import com.neuromove.backend.domain.fsm.entity.FsmState;
import com.neuromove.backend.domain.fsm.entity.enums.FsmStateType;
import com.neuromove.backend.domain.fsm.repository.FsmStateRepository;
import com.neuromove.backend.domain.session.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FsmService {

    private final FsmStateRepository fsmStateRepository;

    public FsmStateType getCurrentState(Session session) {
        return fsmStateRepository.findTopBySessionOrderByTransitionedAtDesc(session)
                .map(FsmState::getToState)
                .orElse(FsmStateType.DRIVING);
    }

    @Transactional
    public void transition(Session session, FsmStateType toState, String reason) {
        FsmStateType fromState = getCurrentState(session);

        if (fromState == toState) {
            return;
        }

        FsmState fsmState = FsmState.builder()
                .session(session)
                .fromState(fromState)
                .toState(toState)
                .reason(reason)
                .build();

        fsmStateRepository.save(fsmState);
    }
}
