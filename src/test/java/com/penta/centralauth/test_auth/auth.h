#ifndef IDSCAN_H

#include "canCom1.h"

struct CanMsgBuf
{
	flexcan_msgbuff_t msgBuf;
	int16_t inst;
	int16_t recvReq;
};