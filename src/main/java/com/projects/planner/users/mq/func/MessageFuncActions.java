package com.projects.planner.users.mq.func;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
@Getter
@RequiredArgsConstructor
public class MessageFuncActions {

    //Channel for message exchange
    private final MessageFunc messageFunc;

    public void setDefaultUserData(Long id) {
        messageFunc.getInnerBus().emitNext(
                MessageBuilder.withPayload(id).build(),
                Sinks.EmitFailureHandler.FAIL_FAST
        );
    }

}
