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
            }}
        });
    },
});