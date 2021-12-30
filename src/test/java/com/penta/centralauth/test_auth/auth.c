flexcan_data_info_t RXdataInfo =
{
		.data_length = 8U,
		.msg_id_type = FLEXCAN_MSG_ID_STD,
		.enable_brs  = true,
		.fd_enable   = true,
		.fd_padding  = 0U
};

flexcan_data_info_t RXdataInfo64 =
{
		.data_length = 64U,
		.msg_id_type = FLEXCAN_MSG_ID_STD,
		.enable_brs  = true,
		.fd_enable   = true,
		.fd_padding  = 0U
};

flexcan_data_info_t TXdataInfo =
{
	   .data_length = 8U,
	   .msg_id_type = FLEXCAN_MSG_ID_STD,
	   .enable_brs  = true,
	   .fd_enable   = true,
	   .fd_padding  = 0U
};

uint8_t Tx_Buffer[64] = {0, };
uint16_t CANtimeout = 0;

void SendCanData(uint8_t inst, uint32_t mailbox, uint32_t messageId, uint8_t * data, uint32_t len)
{
  flexcan_data_info_t dataInfo =
    {
            .data_length = len,
            .msg_id_type = FLEXCAN_MSG_ID_STD,
            .enable_brs  = true,
            .fd_enable   = true,
            .fd_padding  = 0U
    };

    if(messageId != 0x4c1 || messageId != 0x4c2)
    {
    	while(FLEXCAN_DRV_Send(inst, mailbox, &dataInfo, messageId, data) == STATUS_BUSY && CANtimeout < 1000)
    	{
    		CANtimeout++;
    	}
         else
    {
    	while(FLEXCAN_DRV_Send(inst, mailbox, &dataInfo, messageId, data) == STATUS_BUSY);
    }

    }
    CANtimeout = 0;

void CAN_TJA1043T_Enable(void)
{
    PINS_DRV_WritePin(PTB, 3, 1);
}

}