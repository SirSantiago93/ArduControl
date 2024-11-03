$(document).ready(function(){
    
    $(".logo").hover(function(){
        $(this).css("background-color","#575757").css("borderRadius","4px")
    }, function(){
        $(this).css("background-color","").css("borderRadius","")
    })

    $('form input').focus(function() {
            if (!$(this).data('focused')) {
                $(this).val('').css('color', 'black');
                $(this).data('focused', true);
            }
        }).blur(function() {
            if ($(this).val() === '') {
                $(this).css('color', '#6c757d')
                $(this).data('focused', false);
            }
        });
})