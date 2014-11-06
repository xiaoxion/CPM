//
//  RegisterViewController.m
//  GrocerList
//
//  Created by Esau Rubio on 11/5/14.
//  Copyright (c) 2014 Strtatazima. All rights reserved.
//

#import "RegisterViewController.h"
#import "CRToast.h"
#import <Parse/Parse.h>

@interface RegisterViewController ()

@end

@implementation RegisterViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [email resignFirstResponder];
    return YES;
}

-(IBAction)onClick:(id)sender {
    PFUser *user = [PFUser user];
    user.username = usernameR.text;
    user.password = passwordR.text;
    user.email = email.text;
    
    [user signUpInBackgroundWithBlock:^(BOOL succeeded, NSError *error) {
        if (!error) {
            NSDictionary *options = @{
                                      kCRToastTextKey : @"Success!",
                                      kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                      kCRToastBackgroundColorKey : [UIColor blueColor],
                                      kCRToastAnimationInTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionLeft),
                                      kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionRight)
                                      };
            
            [CRToastManager showNotificationWithOptions:options completionBlock:^{NSLog(@"Completed");}];
            
            [self dismissViewControllerAnimated:YES completion:nil];
        } else {
            NSDictionary *options = @{
                                      kCRToastTextKey : [error userInfo][@"error"],
                                      kCRToastTextAlignmentKey : @(NSTextAlignmentCenter),
                                      kCRToastBackgroundColorKey : [UIColor redColor],
                                      kCRToastAnimationInTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationOutTypeKey : @(CRToastAnimationTypeGravity),
                                      kCRToastAnimationInDirectionKey : @(CRToastAnimationDirectionLeft),
                                      kCRToastAnimationOutDirectionKey : @(CRToastAnimationDirectionRight)
                                      };
            
            [CRToastManager showNotificationWithOptions:options completionBlock:^{NSLog(@"Completed");}];
        }
    }];
}

@end
