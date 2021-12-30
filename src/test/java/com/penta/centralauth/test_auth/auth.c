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