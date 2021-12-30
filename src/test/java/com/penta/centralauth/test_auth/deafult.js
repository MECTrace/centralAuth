$(document).ready(function(){
	$(".action_register").click(function(){
		$.register();
	});

    $(".action_apply_rule").click(function(){
		$.apply_rule();
	});
});

$.extend({
    //add register to do
    "register": function(){
        $.ajax({
            dataType: "html",

            success: function(res){{
                $("#default-modal .modal-content").html(res);
				$("#default-modal").modal();

                $(".action_register_submit").click(function(){
					$.register_submit();	
				});

                $("#register_payload_type").change(function(){
					$(".form_payload").hide();	
					
					var target_id = $(this).val();
					$("#"+target_id).show();
				});

                $('[data-toggle="popover"]').popover({
					"placement":	"left",
					"trigger":		"focus",
				});
            },
                error: function(res){
                    console.log(res);	
                }
            }}
            
        });
    },
});