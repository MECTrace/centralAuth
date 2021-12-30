$(document).ready(function(){
	$(".action_register").click(function(){
		$.register();
	});

    $(".action_apply_rule").click(function(){
		$.apply_rule();
	});
});

$.extend({
	"register": function(){

		$.ajax({
			url: "/rule/list/register",
			dataType: "html",
			type: "POST",
			success: function(res){
				$("#default-modal .modal-content").html(res);
				$("#default-modal").modal();

				// register submit
				$(".action_register_submit").click(function(){
					$.register_submit();	
				});

				// show from payload
				$("#register_payload_type").change(function(){
					$(".form_payload").hide();	
					
					var target_id = $(this).val();
					$("#"+target_id).show();
				});
				
				// popover
				$('[data-toggle="popover"]').popover({
					"placement":	"left",
					"trigger":		"focus",
				});
			},
			error: function(res){
				console.log(res);	
			}
		});
	},

    "register_submit": function(){
        var payload_type = $("#register_payload_type").val();
        var obj = $("#register_wrap .form_payload:visible input:text");

        var new_payload = "";

        new_payload += "rule_order="+$("#register_wrap input[name='rule_order']").val()+"&";
		new_payload += "rule_action="+$("#register_wrap select[name='rule_action']").val()+"&";
    }
});